/* ------------------------------------------------------------------------------------
 * Simulacao discreta do algoritmo de exclusao mutua distribuida Ricart-Agrawala.
 * Como entrada exige ou 2 ou 3 nodos/processos e seus respectivos tempos de requisicao
 * da RC como entrada.
 *         Alexandre Veloso de Matos * Sistemas Distribuidos (CI 721) * 2016-1
 * ----------------------------------------------------------------------------------- */

#include <stdio.h>
#include <stdlib.h>
#include "smpl.h"

//Eventos
#define BROADCAST 1 //disseminacao das requisicoes de acesso a RC
#define REQUEST 2 //requisicao individual de acesso a RC
#define REPLY 3 //coordenacao do envio de msgs de reply
#define ENTER_RC 4 //coordenacao do uso da RC
#define LEAVE_RC 5 //efetiva saida e 'limpeza' da RC
#define EXIT 6 //coordenacao da saida da simulacao

//Estados do nodo/processo
#define REQ_RC 1 //requisitando a RC
#define AT_RC 2 //na RC
#define IDLE 3 //apto a requisitar a RC
#define LEFT_RC 4 //se ja usou a RC


typedef struct {
	int   timestamp, //timestamp dessa mensagem (requisicao/resposta)
		  nodoAspirante, //nodo que envia a msg
		  nodoDestino; //nodo que recebe a msg
	float instaEnvio; //instante em que foi programada a execucao do evento associado
} tmsg;
tmsg *req; //lista de requisicoes de entrada na RC
tmsg *rep; //lista de respostas as requisicoes
 
typedef struct {
    int id, 
		estado, //se o nodo esta na RC, requisitando a RC ou se esta livre para iniciar requisicoes
		tstampRC, //timestamp atual do processo
		timestamp, //timestamp de requisicao de entrada na RC do processo
		qdeReplies, //quantos replies foram recebidos ate o momento por um nodo
		qdePendentes, //quantos replies pendentes ate o momento para um nodo
		*pendentes; //lista de todos nodos que possuem um reply pendente para um nodo
} tnodo;
tnodo *nodo; //lista de nodos/processos

int 	N, //quantidade de nodos/processos interessados em disputa pela RC	 
		qdeNodosRC = 0,	//quantos nodos/processos jah tiveram ingresso na RC
		qdeRC = 0, //quantos nodos/processos estao, atualmente, na RC
		qdeMsgs = 0, //quantas msgs de requisicao foram, ate o momento trocadas
		qdeReps = 0, //quantos replies foram, ate o momento trocados
		reqCount = 0, //contador usado para lidar com tarefas de busca/atualizacao da lista de requisicoes
		repCount = 0; //contador usado para lidar com tarefas de busca/atualizada da lista de respostas(replies)	

float 	*tempos; //tempos de broadcast, do interesse de entrada de cada processo na RC, capturados na chamada da simulacao
double  tempoLimite = 0.0; //o tempo limite eh alterado a medida que um processo entra na RC
int 	relogio = 1; //controle do relogio logico global

/* -------------------------------------------------------------------------------------------
 * Inicializacao das estruturas e variaveis usadas na simulacao
 * ------------------------------------------------------------------------------------------ */		
void inicializa() {
	static char fa_name[5]; 
	static int i,j;
	
    printf("==============================================================================\n");
    printf("      Exclusao Mutua Distribuida baseada em Ricart-Agrawala \n");
    printf("      Alexandre Veloso de Matos - Sistemas Distribuidos (2016-1)\n");
    printf("==============================================================================\n");
   	
    nodo = (tnodo *) malloc(sizeof(tnodo)*N);
    req = (tmsg *) malloc(sizeof(tmsg)*(3*(N-1))); //na pior das hipoteses, considera-se que ocorrem (3*(N-1)) mensagens decorrentes das requisicoes a RC
    rep = (tmsg *) malloc(sizeof(tmsg)*(3*(N-1))); //da mesma forma, considera-se que ocorrem (3*(N-1))respostas decorrentes das requisicoes a RC
    
    for (i=0; i<N; i++) {
        memset(fa_name, '\0', 5);
        sprintf(fa_name, "%d", i);
        
        nodo[i].id = facility(fa_name, 1);       
        nodo[i].qdeReplies = 0;
        nodo[i].qdePendentes = 0;
		nodo[i].estado = IDLE;
		nodo[i].tstampRC = 0;
		nodo[i].timestamp = 0; //timestamp em que ocorreu a requisicao a RC
         
        //considera-se que cada nodo requisita apenas uma vez sua entrada na RC, por isso uma Ãºnica lista de pendentes
        nodo[i].pendentes = (int *) malloc(sizeof(int)*N);      
        
        for (j=0; j<N; j++) 
			nodo[i].pendentes[j] = -1; //o valor -1 na posicao j indica que o nodo i nao possui pendencia de reply
    }	
    
    for(i=0; i<3*(N-1); i++) {
		req[i].nodoAspirante='\0'; rep[i].nodoAspirante ='\0';
		req[i].nodoDestino ='\0'; rep[i].nodoDestino ='\0';
		req[i].timestamp = 0; rep[i].timestamp = 0;
		req[i].instaEnvio = 0.0; rep[i].instaEnvio = 0.0;
	}
}

/* -------------------------------------------------------------------------------------------
 * retorna um numero de ponto flutuante entre 1 e 5(inclusive)	
 * ------------------------------------------------------------------------------------------ */
float tempoAleatorio() {
	return (float) uniform(1,6);
}

/* -------------------------------------------------------------------------------------------
 * registra todas as informacoes de resposta as mensagens de requisicoes para guiar a tomada 
 * de decisoes sobre entrada na RC
 * ------------------------------------------------------------------------------------------ */
void nodoRep (int aspirante, int destino, float instante) {

	rep[repCount].nodoAspirante = aspirante;
	rep[repCount].nodoDestino = destino;	
	
	/* sempre que uma resposta(reply) precisa ser enviada, seu envio eh agendado e registrado na lista de respostas e para
	 * ser identificado o destino para onde o reply deve ser enviado,  o tempo aleatorio (instante) eh somado ao tempo atual */ 	
	rep[repCount].instaEnvio = instante;	
	if (repCount > 0) {
		if (rep[repCount].timestamp < nodo[destino].timestamp) nodo[destino].timestamp++;
		else nodo[destino].timestamp = rep[repCount].timestamp + 1;
	}
	
	repCount++;
	qdeReps++;
}

/* -------------------------------------------------------------------------------------------
 * registra todas as requisicoes de entrada na RC e respectivas informacoes
 * ------------------------------------------------------------------------------------------ */
void nodoReqRC(int aspirante, int destino, float instante) {
	
	req[reqCount].nodoAspirante = aspirante;
	req[reqCount].nodoDestino = destino;
	req[reqCount].timestamp = relogio;
	req[reqCount].instaEnvio = time()+instante;	
	nodo[aspirante].estado = REQ_RC;
	printf("[%4.1f]\tProcesso(%d) enviou REQUEST de uso da RC para o Processo(%d). Timestamp = %d\n\n", time(), aspirante, destino, nodo[aspirante].timestamp);
	
	if (reqCount > 0) {
		if (req[reqCount].timestamp < nodo[destino].timestamp) nodo[destino].timestamp++;
		else nodo[destino].timestamp = req[reqCount].timestamp + 1;
	}	
	qdeMsgs++;
	reqCount++;
}

/* -------------------------------------------------------------------------------------------
 * para cada processo do ambiente simulado, escalona uma requisicao de entrada na RC, 
 * disparando requisicoes para os outros nodos 
 * ------------------------------------------------------------------------------------------ */
void broadcast(int processo) {

	double quando;
	int	   destino = 0;	

	nodo[processo].tstampRC = relogio;
	nodo[processo].timestamp = nodo[processo].tstampRC;
	nodo[processo].estado = REQ_RC;			
	while (destino < N) {		
		if (destino != processo) { 
			quando = tempoAleatorio();
			
			nodoReqRC(processo, destino, quando); // registra que o processo aspirante enviou um pedido para entrada na RC 
			
			if (qdeMsgs > 1)
				schedule(REQUEST,quando,destino); //um evento de REQUEST ocorrerah a partir desse instante em 'quando' unidades de tempo
			else {
				nodoRep(destino, processo,time()+1.0);
				schedule(REPLY,1.0,processo);
			}
		}
	destino++;
	}
}

/* -------------------------------------------------------------------------------------------
 * identifica, dentre as mensagens direcionadas como respostas(reply), exatamente a que foi 
 * direcionada a receptor.  * Para que ocorra uma identificacao inequivoca, ao inves de 
 * comparar duas variaveis em ponto flutuante (o que pode * causar resultados inesperados), 
 * eh obtido o resultado absoluto da subtracao dos tempos avaliados.
 * ------------------------------------------------------------------------------------------ */
int idOrigReply (int receptor, float instante) {
 
	int i, origReply;
	float difAtual, difAnterior = 1.0;
	
	for (i=0; i<qdeReps; i++) {
		if (rep[i].nodoDestino == receptor) {
			difAtual = fabs(rep[i].instaEnvio - instante);
			
			if (difAtual <= difAnterior) {
				origReply = rep[i].nodoAspirante;
				difAnterior = difAtual;
			}
		}
	}
	
	return origReply;
}

/* -------------------------------------------------------------------------------------------
 * identifica, dentre as mensagens direcionadas como requisicao, exatamente a que foi direcionada 
 * a receptor. Para que ocorra uma identificacao inequivoca, ao inves de comparar duas variaveis 
 * em ponto flutuante (o que pode causar resultados inesperados), eh obtido o resultado absoluto 
 * da subtracao dos tempos avaliados. 
 * ------------------------------------------------------------------------------------------ */
int idOriginador(int receptor, float instante) {

	int i, idMsg, originador;
	float difAtual, difAnterior = 1.0;
	
	for(i=0;i<qdeMsgs;i++) {
		if (req[i].nodoDestino == receptor) {
			difAtual = fabs(req[i].instaEnvio - instante);
			
			if (difAtual < difAnterior) {
				originador = req[i].nodoAspirante;
				difAnterior = difAtual;
				idMsg = i;
			}
		}		
	}
	return originador;
}

/* -------------------------------------------------------------------------------------------
 * coordena as mensagens de requisicao que sao enviadas. Permite determinar se um nodo vai 
 * escalonar uma resposta ou manter essa pendencia de permissao ate que tenha condicoes de
 * trata-las.
 * ------------------------------------------------------------------------------------------ */
void avaliaReq(int originador, int receptor, float instante){
	int 	i, //contador usado para ajudar a avaliar a prioridade de outras requisicoes
			prior; //armazena a prioridade ou timestamp do nodo que originou a requisicao, para posteriormente ser comparado
		
	float quando; //armazena um tempo aleatorio, usado para escalonar o tempo de execucao de um reply	
	
	prior = nodo[originador].timestamp;
	
	//identificar se o receptor da mensagem ja esta na RC		
	if (status(nodo[receptor].id) != 0) {
		printf("........ reply pendente: de %d para %d\n\n",originador, receptor);
		nodo[receptor].pendentes[originador] = 1;
		nodo[receptor].qdePendentes++;
	}

	//identificar se ha alguma outra msg com maior prioridade ou originada por originador de menor ordem
	else { 
		i = 0;
		while (nodo[i].timestamp > prior || i <= N) i++;

		if (i >= N) { //entao nao ha msg com menor prioridade
		
			// portanto, em 'quando' unidades de tempo, a partir desse instante, eh escalonada um envio de msg reply
			quando = tempoAleatorio();
			nodoRep(receptor, originador, time()+quando); 
			//para controlar a execucao do evento REPLY, a funcao nodoRep permite que todas msgs de reply sejam armazenadas e posteriormente recuperadas
			schedule(REPLY, quando, originador); 
		}
		else {
			//reqCount = 0; //depois de todas requisicoes terem sido armazenadas, reqCount serah usada para controlar o armazenamento das msgs de resposta
			if (nodo[i].timestamp < prior ) { //ha uma requisiÃ§Ã£o com menor timestamp, portanto, adicionar req de originador no vetor de pendentes de receptor
				printf("........ reply pendente: de %d para %d\n\n",originador, receptor);
				nodo[receptor].pendentes[originador] = 1;
				nodo[receptor].qdePendentes++;
			}
			if (nodo[i].timestamp == prior) { //averiguar se a ordem do requisitante de timestamp encontrado eh menor

				if (receptor < originador ) {
					printf("........ reply pendente: de %d para %d\n\n",originador, receptor);
					nodo[receptor].pendentes[originador] = 1;
					nodo[receptor].qdePendentes++;
				}
				else {								
					// em 'quando' unidades de tempo, a partir desse instante, eh escalonado um envio de msg reply
					quando = tempoAleatorio();
					nodoRep(receptor, originador, time()+quando);
					schedule(REPLY, quando, originador); //da parte do receptor estah tudo ok para que originador entre na RC
				}
			}
		}
	}
}

/* -------------------------------------------------------------------------------------------
 * a partir da identificacao do nodo que enviou a msg de reply, checar se o receptor estah apto 
 * a entrar na RC
 * ------------------------------------------------------------------------------------------ */
void recebeMsg(int originador, int receptor, double instante) {
	
	nodo[originador].qdeReplies++;
	printf("[%4.1f]\tProcesso(%d) recebeu REPLY do Processo(%d)\n\n", instante, originador, receptor);	
	if (qdeRC == 0) {
		if (nodo[originador].qdeReplies >= (N-1) && status(nodo[originador].id == 0)) { //apenas se verifica nodos livres (que ja nao estejam na RC)
			nodo[originador].estado = AT_RC;
			qdeRC++;
			schedule(ENTER_RC, 0.0, originador); 
		}
	}
}

/* -------------------------------------------------------------------------------------------
 * para cada pendÃªncia de permissao que processo ainda mantem, deve-se escalonar uma resposta
 * ------------------------------------------------------------------------------------------ */
void retiradaRC(int processo)  {

	int i;
	float quando = 0.0;
	
	//se durante a estadia de um processo na RC outro(s) processo(s) ja estava(m) em condicoes de entrar na RC, deve-se verificar a permissao
	for (i=0; i<N; i++) {
		if (nodo[i].estado == REQ_RC && nodo[i].qdeReplies >= (N-1) && qdeRC == 0) {
			qdeRC++;
			schedule(ENTER_RC,quando,i);
			quando = 0.0;
		}
	}
		
	if (nodo[processo].qdePendentes > 0) {
		
		for (i=0; i<N; i++) {
			if (nodo[processo].pendentes[i] == 1) {
				printf(" ...... ha uma pendencia de permissao de %d para %d, escalonando msg\n\n", i,processo);
				nodo[processo].pendentes[i] = -1; //a medida que uma permissao eh tratada, ela eh retirada da lista de pendencias
				nodo[processo].qdePendentes--;
				
				quando = tempoAleatorio();
				nodoRep(processo, i, time()+quando);
				schedule(REPLY,quando, i); 
			}
		}
	}
	
}

/* -------------------------------------------------------------------------------------------
 * todos os nodos jah passaram e sairam da RC e, portanto, a simulacao pode encerrar.
 * ------------------------------------------------------------------------------------------ */
void sair() {
	 
	printf("----------------------------------------------------------------------------\n");
	printf("Todos os processos passaram pela RC. Encerramento da simulacao.\n");
	printf("----------------------------------------------------------------------------\n");
	exit(1);
}

void aviso() {
	printf("===========================================================================================================\n");
	printf("Simulacao de Exclusao Mutua baseada em Ricart-Agrawala. Minimo: 2 processos. Maximo: 3 processos.\n");
	printf("Modo correto de uso: <<qde. processos[2/3]>> <<tempo 1 tempo 2 ... tempo n>>\n");
	printf("tempo(i) = para cada processo, eh o instante de tempo em que o respectivo processo requisita entrada na RC\n");
	printf("===========================================================================================================\n");
}


int main(int argc, char *argv[]){
	static int  token, 
				event,
				i, 
				origem,
				entrada = 0;
				  
	if (argc >= 4) {
		N = (int)atoi(argv[1]); //qde de nodos que participam da simulacao
		
		if (N == 2 || N == 3) {
			if (N == (argc-2)) { //evita que algum nodo fique sem tempo associado
				tempos = (float *) malloc(sizeof(float)*N);
			
				for (i=0; i<N; i++)
					tempos[i] = atof(argv[i+2]);
					
				entrada = 1;				
			}
		}
	}

	if (!entrada) {
		aviso();
		exit(1);
	}
	
	smpl(0,"Ricart-Agrawala");
	reset();
	stream(1);
	
	inicializa();
	
	srand((unsigned)time(NULL));
	
	for (i=0; i<N; i++) 
		schedule(BROADCAST, tempos[i], i);
		
	while (qdeNodosRC <= N){
		cause (&event, &token);
	
		
	switch(event) {

		case BROADCAST: //esse evento eh responsavel por disparar, para cada processo, sua requisicao de entrada na RC, a todo outro processo
			printf("[%4.1f]\tProcesso(%d) requisita entrada na RC \n\n",time(),token);
			broadcast(token);	
		break;
		
		case REQUEST: //apos o broadcast, REQUEST lida com o envio e o registro das requisicoes de acesso a RC individuais
			relogio++;
			origem = idOriginador(token, time()); //que nodo enviou a msg para token no tempo time()
			if (origem!= token) avaliaReq(origem, token, time());
			
		break;
		
		case REPLY: //ao longo das requisicoes de acesso, processos podem estar a aptos a responder com permissoes a outros processos
			relogio++;
			origem = idOrigReply(token, time());
			if (origem!= token) recebeMsg(token, origem, time());
		break;
		
		case ENTER_RC: //a entrada na RC da-se apenas por 10 unidades de tempo, logo apos, escalona-se uma saida da RC
			qdeNodosRC++; 
			nodo[token].estado = AT_RC;
			printf("[%4.1f]\tProcesso(%d) entrou na RC \n\n",time(), token);
			tempoLimite = time()+1.0;
			schedule(LEAVE_RC, 1.0, token);			
		break;
		
		case LEAVE_RC: //a saida da RC eh a verificacao de pendencias de permissoes
			printf("[%4.1f] \tProcesso(%d) sai da RC\n\n",time(), token);
			nodo[token].estado = LEFT_RC;
			qdeRC--;
			if (qdeNodosRC >= N && time() <= tempoLimite) schedule(EXIT, tempoLimite, token);
			else retiradaRC(token);
		break;
		
		case EXIT:
			sair();
		break;
		
	} //switch
	} //while
}
