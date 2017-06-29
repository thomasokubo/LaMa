/*MC302 - Trabalho 2
* Nome: Thomas Taichi Okubo 
* RA: 148081
*/

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;

public class MotorRA148081 extends Motor {

	public MotorRA148081(Baralho deck1, Baralho deck2, ArrayList<Carta> mao1,
			ArrayList<Carta> mao2, Jogador jogador1, Jogador jogador2,
			int verbose, int tempoLimitado, PrintWriter saidaArquivo, EnumSet<Funcionalidade> funcionalidadesAtivas) {
		super(deck1, deck2, mao1, mao2, jogador1, jogador2, verbose,
				tempoLimitado, saidaArquivo, funcionalidadesAtivas);
		imprimir("========================");
		imprimir("*** Classe "+this.getClass().getName()+" inicializada.");
		imprimir("Funcionalidade ativas no Motor:");
		imprimir("INVESTIDA: "+(this.funcionalidadesAtivas.contains(Funcionalidade.INVESTIDA)?"SIM":"NAO"));
		imprimir("ATAQUE_DUPLO: "+(this.funcionalidadesAtivas.contains(Funcionalidade.ATAQUE_DUPLO)?"SIM":"NAO"));
		imprimir("PROVOCAR: "+(this.funcionalidadesAtivas.contains(Funcionalidade.PROVOCAR)?"SIM":"NAO"));
		imprimir("========================");
	}
	
	private int jogador; // 1 == turno do jogador1, 2 == turno do jogador2.
	private int turno;
	private int nCartasHeroi1;
	private int nCartasHeroi2;
	
	private Mesa mesa;
	
	// "Apontadores" - Assim podemos programar genericamente os métodos para funcionar com ambos os jogadores
	private ArrayList<Carta> mao;
	private ArrayList<Carta> lacaios;
	private ArrayList<Carta> lacaiosOponente;
	
	// "Memória" - Para marcar ações que só podem ser realizadas uma vez por turno.
	private boolean poderHeroicoUsado;
	private HashSet<Integer> lacaiosAtacaramID;
	private HashSet<Integer> lacaiosGolpeDuplo;


	
	@Override
	public int executarPartida() throws LamaException {
		vidaHeroi1 = vidaHeroi2 = 30;
		manaJogador1 = manaJogador2 = 1;
		nCartasHeroi1 = cartasIniJogador1; 
		nCartasHeroi2 = cartasIniJogador2;
		ArrayList<Jogada> movimentos = new ArrayList<Jogada>();
		int noCardDmgCounter1 = 1;
		int noCardDmgCounter2 = 1;
		turno = 1;
		
		for(int k = 0; k < 60; k++){
			imprimir("\n=== TURNO "+turno+" ===\n");
			// Atualiza mesa (com cópia profunda)
			@SuppressWarnings("unchecked")
			ArrayList<CartaLacaio> lacaios1clone = (ArrayList<CartaLacaio>) UnoptimizedDeepCopy.copy(lacaiosMesa1);
			@SuppressWarnings("unchecked")
			ArrayList<CartaLacaio> lacaios2clone = (ArrayList<CartaLacaio>) UnoptimizedDeepCopy.copy(lacaiosMesa2);
			mesa = new Mesa(lacaios1clone, lacaios2clone, vidaHeroi1, vidaHeroi2, nCartasHeroi1+1, nCartasHeroi2, turno>10?10:turno, turno>10?10:(turno==1?2:turno));
			
			// Apontadores para jogador1
			mao = maoJogador1;
			lacaios = lacaiosMesa1;
			lacaiosOponente = lacaiosMesa2;
			jogador = 1;
			
			// Processa o turno 1 do Jogador1
			imprimir("\n----------------------- Começo de turno para Jogador 1:");
			long startTime, endTime, totalTime;
			
			// Cópia profunda de jogadas realizadas.
			@SuppressWarnings("unchecked")
			ArrayList<Jogada> cloneMovimentos1 = (ArrayList<Jogada>) UnoptimizedDeepCopy.copy(movimentos);
			
			startTime = System.nanoTime();
			if( baralho1.getCartas().size() > 0){
				if(nCartasHeroi1 >= maxCartasMao){
					movimentos = jogador1.processarTurno(mesa, null, cloneMovimentos1);
					comprarCarta(); // carta descartada
				}
				else
					movimentos = jogador1.processarTurno(mesa, comprarCarta(), cloneMovimentos1);
			}
			else{
				imprimir("Fadiga: O Herói 1 recebeu "+noCardDmgCounter1+" de dano por falta de cartas no baralho. (Vida restante: "+(vidaHeroi1-noCardDmgCounter1)+").");
				vidaHeroi1 -= noCardDmgCounter1++;
				if( vidaHeroi1 <= 0){
					// Jogador 2 venceu
					imprimir("O jogador 2 venceu porque o jogador 1 recebeu um dano mortal por falta de cartas ! (Dano : " +(noCardDmgCounter1-1)+ ", Vida Herï¿½i 1: "+vidaHeroi1+")");
					return 2;
				}
				movimentos = jogador1.processarTurno(mesa, null, cloneMovimentos1);
			}
			endTime = System.nanoTime();
			totalTime = endTime - startTime;
			if( tempoLimitado!=0 && totalTime > 3e8){ // 300ms
				// Jogador 2 venceu, Jogador 1 excedeu limite de tempo
				return 2;
			}
			else
				imprimir("Tempo usado em processarTurno(): "+totalTime/1e6+"ms");

			// Começa a processar jogadas do Jogador 1
			this.poderHeroicoUsado = false;
            this.lacaiosAtacaramID = new HashSet<Integer>();
            this.lacaiosGolpeDuplo = new HashSet<Integer>();
			for(int i = 0; i < movimentos.size(); i++){
				processarJogada (movimentos.get(i));
			}
			
			if( vidaHeroi2 <= 0){
				// Jogador 1 venceu
				return 1;
			}
			
			// Atualiza mesa (com cópia profunda)
			@SuppressWarnings("unchecked")
			ArrayList<CartaLacaio> lacaios1clone2 = (ArrayList<CartaLacaio>) UnoptimizedDeepCopy.copy(lacaiosMesa1);
			@SuppressWarnings("unchecked")
			ArrayList<CartaLacaio> lacaios2clone2 = (ArrayList<CartaLacaio>) UnoptimizedDeepCopy.copy(lacaiosMesa2);
			mesa = new Mesa(lacaios1clone2, lacaios2clone2, vidaHeroi1, vidaHeroi2, nCartasHeroi1, nCartasHeroi2+1, turno>10?10:turno, turno>10?10:(turno==1?2:turno));
			
			// Apontadores para jogador2
			mao = maoJogador2;
			lacaios = lacaiosMesa2;
			lacaiosOponente = lacaiosMesa1;
			jogador = 2;
			
			// Processa o turno 1 do Jogador2
			imprimir("\n\n----------------------- Começo de turno para Jogador 2:");
			
			// Cópia profunda de jogadas realizadas.
			@SuppressWarnings("unchecked")
			ArrayList<Jogada> cloneMovimentos2 = (ArrayList<Jogada>) UnoptimizedDeepCopy.copy(movimentos);
			
			startTime = System.nanoTime();

			
			if( baralho2.getCartas().size() > 0){
				if(nCartasHeroi2 >= maxCartasMao){
					movimentos = jogador2.processarTurno(mesa, null, cloneMovimentos2);
					comprarCarta(); // carta descartada
				}
				else
					movimentos = jogador2.processarTurno(mesa, comprarCarta(), cloneMovimentos2);
			}
			else{
				imprimir("Fadiga: O Herói 2 recebeu "+noCardDmgCounter2+" de dano por falta de cartas no baralho. (Vida restante: "+(vidaHeroi2-noCardDmgCounter2)+").");
				vidaHeroi2 -= noCardDmgCounter2++;
				if( vidaHeroi2 <= 0){
					// Vitoria do jogador 1
					imprimir("O jogador 1 venceu porque o jogador 2 recebeu um dano mortal por falta de cartas ! (Dano : " +(noCardDmgCounter2-1)+ ", Vida Herï¿½i 2: "+vidaHeroi2 +")");
					return 1;
				}
				movimentos = jogador2.processarTurno(mesa, null, cloneMovimentos2);
			}
			
			endTime = System.nanoTime();
			totalTime = endTime - startTime;
			if( tempoLimitado!=0 && totalTime > 3e8){ // 300ms
				// Limite de tempo pelo jogador 2. Vitoria do jogador 1.
				return 1;
			}
			else
				imprimir("Tempo usado em processarTurno(): "+totalTime/1e6+"ms");
			
			this.poderHeroicoUsado = false;
            this.lacaiosAtacaramID = new HashSet<Integer>();
			for(int i = 0; i < movimentos.size(); i++){
				processarJogada (movimentos.get(i));
			}
			if( vidaHeroi1 <= 0){
				// Vitoria do jogador 2
				return 2;
			}
			turno++;
		}
		
		// Nunca vai chegar aqui dependendo do número de rodadas
		imprimir("Erro: A partida não pode ser determinada em mais de 60 rounds. Provavel BUG.");
		throw new LamaException(-1, null, "Erro desconhecido. Mais de 60 turnos sem termino do jogo.", 0);
	}

	private class PlayerState {
		
		private int life;
		private int mana;
		
		PlayerState(int life, int mana){
			this.life = life;
			this.mana = mana;
		}
		
		void takeDamage(int damage){
			this.life-=damage;
		}
		
		void spendMana(int manaCost){
			this.mana -= manaCost;
		}
		
	}
	
	
	protected void processarJogada(Jogada umaJogada) throws LamaException {
		CartaLacaio lacaioAlvo;
		CartaLacaio lacaioAtacante;
		Optional<CartaLacaio> lacaioProvocante;

		this.manaJogador1 = mesa.getManaJog1();
		this.manaJogador2 = mesa.getManaJog2();
		PlayerState attacker, target;
		if(jogador==1){
			attacker = new PlayerState(this.vidaHeroi1, this.manaJogador1);
			target = new PlayerState(this.vidaHeroi2, this.manaJogador2);
		} else {
			attacker = new PlayerState(this.vidaHeroi2, this.manaJogador2);
			target = new PlayerState(this.vidaHeroi1, this.manaJogador1);
		}

		switch(umaJogada.getTipo()){
		case ATAQUE:
		
			// Verifica de o lacaio a atacar eh valido
			if(!(lacaios.contains(umaJogada.getCartaJogada()))){
				String erroMensagem = "Erro: Tentou-se atacar com o lacaio carta_id=" + umaJogada.getCartaJogada().getID() + " mas a carta eh invalida";
				imprimir(erroMensagem);
				throw new LamaException(5, umaJogada, erroMensagem, jogador==1?2:1);
			}
				
			lacaioAtacante = (CartaLacaio) lacaios.get(lacaios.indexOf(umaJogada.getCartaJogada()));

			if(!funcionalidadesAtivas.contains(Funcionalidade.INVESTIDA)){
				// Se INVESTIDA estiver desabilitado, qualquer tentativa de se atacar com um lacaio
				// que acaobou de ser baixado deve gerar uma exceçao
				if(lacaioAtacante.getTurno()==this.turno){
					String erroMensagem = "Erro: Tentou-se atacar com o lacaio carta_id=" + umaJogada.getCartaJogada().getID() + " no turno em que foi baixado.";
					imprimir(erroMensagem);
					throw new LamaException(6, umaJogada, erroMensagem, jogador==1?2:1);
					
				}
			} else {
				// Se INVESTIDA estiver habilitada, mas o lacaio que estiver tentando atacar no turno em que
				// foi baixado nao tiver INVESTIDA, deve-se gerar exceçao
				if(lacaioAtacante.getTurno()==this.turno && !(lacaioAtacante.getEfeito().equals(TipoEfeito.INVESTIDA))){
					String erroMensagem = "Erro: Tentou-se atacar com o lacaio carta_id=" + umaJogada.getCartaJogada().getID() + " no turno em que foi baixado, sem que ele tenha INVESTIDA";
					imprimir(erroMensagem);
					throw new LamaException(6, umaJogada, erroMensagem, jogador==1?2:1);
				}
			}
			
			
			if(funcionalidadesAtivas.contains(Funcionalidade.ATAQUE_DUPLO)){
				// Se um lacaio ja atacou e não tem ATAQUE DUPLO, deve-se gerar exeção 
				if(lacaiosAtacaramID.contains(lacaioAtacante.getID()) && !(lacaioAtacante.getEfeito().equals(TipoEfeito.ATAQUE_DUPLO))){
					String erroMensagem = "Erro: Tentou-se atacar novamente com o lacaio carta_id=" + umaJogada.getCartaJogada().getID() + " mas a carta não tem ATAQUE DUPLO";
					imprimir(erroMensagem);
					throw new LamaException(7, umaJogada, erroMensagem, jogador==1?2:1);

				}
				// Se o lacaio tem ATAQUE DUPLO, mas ja atacou duas vezes, deve-se gerar exceção
				if(lacaiosAtacaramID.contains(lacaioAtacante.getID()) && lacaioAtacante.getEfeito().equals(TipoEfeito.ATAQUE_DUPLO)){
					if(lacaiosGolpeDuplo.contains(lacaioAtacante.getID())){
						String erroMensagem = "Erro: Tentou-se atacar pela terceira vez com o lacaio carta_id=" + umaJogada.getCartaJogada().getID() + ", com ATAQUE DUPLO";
						imprimir(erroMensagem);
						throw new LamaException(7, umaJogada, erroMensagem, jogador==1?2:1);
					}
				}
			} else {
				// Se ATAQUE DUPLO estiver desabilitado, qualquer tentativa de se atacar duas vezes no mesmo turno
				// deve gerar uma exceção
				if(lacaiosAtacaramID.contains(lacaioAtacante.getID())){
					String erroMensagem = "Erro: Tentou-se atacar mais de uma vez com o lacaio carta_id=" + umaJogada.getCartaJogada().getID() ;
					imprimir(erroMensagem);
					throw new LamaException(7, umaJogada, erroMensagem, jogador==1?2:1);
				}
			}

			
			// Caso o alvo seja o Heroi oponente
			if(umaJogada.getCartaAlvo() == null){
				if(funcionalidadesAtivas.contains(Funcionalidade.PROVOCAR)){
					lacaioProvocante = lacaiosOponente.stream()
							.map(c -> (CartaLacaio) c)
							.filter(c -> c.getEfeito().equals(TipoEfeito.PROVOCAR))
							.findFirst();

					// Atacar um Heroi, sendo que há lacaio(s) com PROVOCAR em campo, gera uma exceçao
					if(lacaioProvocante.isPresent()){
						String erroMensagem = "Erro: O alvo Heroi"+ (jogador==1?2:1) + " nao eh um alvo valido, pois o lacaio carta_id=" + lacaioProvocante.get().getID() + " tem PROVOCAR";
						imprimir(erroMensagem);
						throw new LamaException(13, umaJogada, erroMensagem, jogador==1?2:1);		
					} 
				} 
					
				imprimir("JOGADA: O lacaio_id="+lacaioAtacante.getID()+" atacou o Heroi" + (jogador==1?2:1) +  ".");
				target.takeDamage(lacaioAtacante.getAtaque());
								
			// Se o alvo for um lacaio
			} else {
	
				try{
					lacaioAlvo = (CartaLacaio) lacaiosOponente.get(lacaiosOponente.indexOf(umaJogada.getCartaAlvo()));
					if(funcionalidadesAtivas.contains(Funcionalidade.PROVOCAR)){
						lacaioProvocante = lacaiosOponente.stream()
								.map(c -> (CartaLacaio) c)
								.filter(c -> c.getEfeito().equals(TipoEfeito.PROVOCAR))
								.findFirst();
	

						// Atacar um lacaio que nao tem PROVOCAR sendo que ha algum lacaio com PROVOCAR em campo (oponente)
						// gera uma exceçao
						if(lacaioProvocante.isPresent() && !lacaioAlvo.getEfeito().equals(TipoEfeito.PROVOCAR)){
							String erroMensagem = "Erro: O lacaio carta_id="+lacaioAlvo.getID() + " não pode ser alvo, pois o lacaio+="+lacaioProvocante.get().getID() + " tem PROVOCAR";
							imprimir(erroMensagem);
							throw new LamaException(13, umaJogada, erroMensagem, jogador==1?2:1);
						}
						
					}
					
					imprimir("JOGADA: O lacaio_id=" + lacaioAtacante.getID() + " ataca lacaio_id="+ lacaioAlvo.getID());
					lacaioAtacante.setVidaAtual(lacaioAtacante.getVidaAtual() - lacaioAlvo.getAtaque());
					lacaioAlvo.setVidaAtual(lacaioAlvo.getVidaAtual() - lacaioAtacante.getAtaque());
					

					if(lacaioAlvo.getVidaAtual() <= 0) {
						imprimir("O lacaio do oponente carta_id="+lacaioAlvo.getID() + " morreu.");
						lacaiosOponente.remove(lacaioAlvo);
					}

					
					
				} catch(IndexOutOfBoundsException ex){
					String erroMensagem = "Erro: Tentou-se atacar com o lacaio carta_id="+umaJogada.getCartaJogada().getID() + " mas o alvo carta_id="+umaJogada.getCartaAlvo().getID() +" não eh valido";
					for(Carta card : lacaiosOponente){
						erroMensagem += card.getID() + ", ";
					}

					imprimir(erroMensagem);
					throw new LamaException(8, umaJogada, erroMensagem, jogador==1?2:1);
					
				}
			}
			
			if(lacaioAtacante.getVidaAtual() <= 0) {
				imprimir("Meu lacaio carta_id="+lacaioAtacante.getID() + " morreu.");
				lacaios.remove(lacaios.indexOf(lacaioAtacante));
			} else {
				if(funcionalidadesAtivas.contains(Funcionalidade.ATAQUE_DUPLO)){
					if(lacaiosAtacaramID.contains(lacaioAtacante.getID())){
						lacaiosGolpeDuplo.add(lacaioAtacante.getID());
					} else {
						lacaiosAtacaramID.add(lacaioAtacante.getID());
					}
				} else {
					lacaiosAtacaramID.add(lacaioAtacante.getID());
				}		
			}
			
			break;
			
		case LACAIO:
	
			int lacaioID = umaJogada.getCartaJogada().getID();
			
			// Verifica se o jogador nao atingiu o numero maximo de lacaios em campo			
			if(lacaios.size() == 7) {
				String erroMensagem = "Erro: Tentou-se usar a carta_id="+lacaioID+ ", porem ja tem sete lacaios em campo";
				imprimir(erroMensagem);
				throw new LamaException(4, umaJogada, erroMensagem, jogador==1?2:1);
			}
			
			// Verifica se o jogador possue a carta a ser jogada em mao
			if(!mao.contains(umaJogada.getCartaJogada())){
				String erroMensagem = "Erro: Tentou-se usar a carta_id="+lacaioID+", porem esta carta nao existe na mao. IDs de cartas na mao: ";
				for(Carta card : mao){
					erroMensagem += card.getID() + ", ";
				}
				imprimir(erroMensagem);
				throw new LamaException(1, umaJogada, erroMensagem, jogador==1?2:1);
			}
			
			Carta lacaioBaixado = mao.get(mao.indexOf(umaJogada.getCartaJogada()));
			
			// Verifica se carta a ser baixada de fato eh do tipo LACAIO
			if(!(lacaioBaixado instanceof CartaLacaio)) {
				String erroMensagem = "Erro: Tentou-se usar a carta" + lacaioBaixado.getNome() + "(ID="+lacaioID + ")"+", porem esta carta nao pertence a classe CartaLacaio";
				imprimir(erroMensagem);
				throw new LamaException(3, umaJogada, erroMensagem, jogador==1?2:1);
			}
			
			// Verifica se o jogador tem mana suficiente para baixar a carta
			if(lacaioBaixado.getMana() > attacker.mana){
				String erroMensagem = "Erro: Tentou-se usar a carta_id="+umaJogada.getCartaJogada().getID()+", porem eh preciso ter " + umaJogada.getCartaJogada().getMana()+" mas ha " + (jogador==1?this.manaJogador1:this.manaJogador2 +" de mana disponivel") ;
				imprimir(erroMensagem);
				throw new LamaException(2, umaJogada, erroMensagem, jogador==1?2:1);
			}
			
			
			imprimir("JOGADA: O lacaio carta_id="+lacaioID+" foi baixado para a mesa.");
			lacaios.add(lacaioBaixado); // lacaio adicionado a mesa
			mao.remove(lacaioBaixado); // lacaio retirado da mao
			attacker.spendMana(lacaioBaixado.getMana());
			break;
			
		case MAGIA:
			// Verifica se a carta a ser baixada se encontra em mao
			if(!mao.contains(umaJogada.getCartaJogada())){
				String erroMensagem = "Erro: Tentou-se baixar a carta_id="+umaJogada.getCartaJogada().getID()+", mas ela nao esta presente na mao";
				for(Carta card : mao){
					erroMensagem += card.getID() + ", ";
				}
				imprimir(erroMensagem);
				throw new LamaException(1, umaJogada, erroMensagem, jogador==1?2:1);
			}
			
			// Verifica se a carta eh de fato do tipo magia
			if(!(umaJogada.getCartaJogada() instanceof CartaMagia)) {
				String erroMensagem = "Erro: Tentou-se baixar a carta_id="+umaJogada.getCartaJogada().getID()+ ", mas ela nao eh magia";					
				imprimir(erroMensagem);
				throw new LamaException(9, umaJogada, erroMensagem, jogador==1?2:1);
			}
									
			
			CartaMagia magia = (CartaMagia) mao.get(mao.indexOf(umaJogada.getCartaJogada()));
			
			// Verifica se o jogador tem mana suficiente para baixar a carta
			if(magia.getMana() > attacker.mana){
				String erroMensagem = "Erro: Tentou-se baixar a carta_id="+umaJogada.getCartaJogada().getID()+ ", de custo de mana igual a " + umaJogada.getCartaJogada().getMana()+", mas hÃ¡ " + (jogador==1?mesa.getManaJog1():mesa.getManaJog2()+" de mana");					
				imprimir(erroMensagem);
				throw new LamaException(2, umaJogada, erroMensagem, jogador==1?2:1);
			}

			switch(magia.getMagiaTipo()){
			case ALVO:
				
				// Dano direto a um heroi
				if(umaJogada.getCartaAlvo() == null){
					imprimir("JOGADA: A magia carta_id="+magia.getID() +" causa " +magia.getMagiaDano()+ " de dano ao Heroi"+(jogador==1?2:1));
					target.takeDamage(magia.getMagiaDano());
					attacker.spendMana(magia.getMana());
				
				// Dano por magia a um lacaio do oponente
				} else {
					// Verifica se o lacaio alvo e valido
					if(!(lacaiosOponente.contains(umaJogada.getCartaAlvo()))){
						String erroMensagem = "Erro: Tentou-se usar a magia carta_id="+umaJogada.getCartaJogada().getID()+", mas a carta_id=" + umaJogada.getCartaAlvo().getID() +" nao e um alvo valido";					
						imprimir(erroMensagem);
						throw new LamaException(10, umaJogada, erroMensagem, jogador==1?2:1);
					}
					
					imprimir("JOGADA: A magia_id="+magia.getID()+" causa "+ magia.getMagiaDano()+" no lacaio_id="+umaJogada.getCartaAlvo()+" do oponente");
					lacaioAlvo = (CartaLacaio)lacaiosOponente.get(lacaiosOponente.indexOf(umaJogada.getCartaAlvo()));
					lacaioAlvo.setVidaAtual(lacaioAlvo.getVidaAtual()-magia.getMagiaDano());

					if(lacaioAlvo.getVidaAtual()<=0) {
						imprimir("O lacaio do oponente carta_id=" + lacaioAlvo.getID() + " morreu");
						lacaiosOponente.remove(lacaioAlvo);
					}

					attacker.spendMana(magia.getMana());
				}
				break;
			
			case AREA:
				imprimir("JOGADA: A magia de dano em area carta_id=" +magia.getID() + ", dano de " + magia.getMagiaDano()+" em todos os lacaios do oponente e ao Heroi"+(jogador==1?2:1));
				lacaiosOponente.stream()
					.map(carta -> (CartaLacaio) carta)
					.forEach(carta->{
						carta.setVidaAtual(carta.getVidaAtual()-magia.getMagiaDano());
					});
				
				for(int i=0;i<lacaiosOponente.size();i++){
					CartaLacaio lac = (CartaLacaio) lacaiosOponente.get(i);
					if(lac.getVidaAtual() <= 0){

						imprimir("O lacaio do oponente carta_id=" + lac.getID() + " morreu.");
						lacaiosOponente.remove(lac);
						i--;
					}
				}
				
				target.takeDamage(magia.getMagiaDano());
				attacker.spendMana(magia.getMana());
				break;
				
			case BUFF:
				// Verifica se o lacaio alvo esta em campo
				if(!(lacaios.contains(umaJogada.getCartaAlvo()))){
					String erroMensagem = "Erro: Tentou-se baixar a carta_id="+umaJogada.getCartaJogada().getID()+", mas a carta_id=" + umaJogada.getCartaAlvo().getID() +" nao e um alvo valido";					
					imprimir(erroMensagem);
					throw new LamaException(10, umaJogada, erroMensagem, jogador==1?2:1);

				}
				
				imprimir("JOGADA: O lacaio_id="+umaJogada.getCartaAlvo()+" recebeu " + magia.getMagiaDano() +" de ataque e de vida.");
				lacaioAlvo = (CartaLacaio) umaJogada.getCartaAlvo();
				lacaioAlvo.setAtaque(lacaioAlvo.getAtaque()+magia.getMagiaDano());
				lacaioAlvo.setVidaAtual(lacaioAlvo.getVidaAtual()+magia.getMagiaDano());
				lacaioAlvo.setVidaMaxima(lacaioAlvo.getVidaMaxima()+magia.getMagiaDano());
				attacker.spendMana(magia.getMana());
				
				break;
			}
			
			break;
			
		case PODER:
			
			// Verifica se ha mana suficiente para usar o poder do heroi 
			if(attacker.mana < 2 ){
				String erroMensagem = "Erro: Tentou-se usar o PODER do Heroi"+jogador+", porém é preciso 2 de mana, e há "+(jogador==1?this.manaJogador1:this.manaJogador2)+" de mana";
				imprimir(erroMensagem);
				throw new LamaException(2, umaJogada, erroMensagem, jogador==1?2:1);
			}
			
			// Verifica se o poder do heroi ja foi usado
			if(poderHeroicoUsado){
				String erroMensagem = "Erro: Não é possível usar o PODER do Heroi"+jogador+" no alvo carta_id="+umaJogada.getCartaAlvo()+ ", pois já o poder do heói já foi usado.";
				imprimir(erroMensagem);
				throw new LamaException(2, umaJogada, erroMensagem, jogador==1?2:1);
			}
			
			// Uso do poder do heroi diretamente no heroi oponente
			if(umaJogada.getCartaAlvo() == null)
				target.takeDamage(1);
			// Uso do poder do heroi em lacaio do oponente
			else {
				// Verifica se o lacaio alvo e valido
				if(!(lacaiosOponente.contains(umaJogada.getCartaAlvo()))) {
					String erroMensagem = "Erro: Não é possível usar o PODER do Heroi"+jogador+" no alvo carta_id="+umaJogada.getCartaAlvo()+ ", pois o alvo é inválido.";
					imprimir(erroMensagem);
					throw new LamaException(2, umaJogada, erroMensagem, jogador==1?2:1);

				}
				
				imprimir("JOGADA: O Heroi"+ jogador + " usou o poder do heroi.");
				lacaioAlvo = (CartaLacaio) lacaiosOponente.get(lacaiosOponente.indexOf(umaJogada.getCartaAlvo()));
				attacker.takeDamage(lacaioAlvo.getAtaque());
				lacaioAlvo.setVidaAtual(lacaioAlvo.getVidaAtual()-1);
				if(lacaioAlvo.getVidaAtual()<=0)
					lacaiosOponente.remove(lacaioAlvo);
					
			}
			
			poderHeroicoUsado=true;
			attacker.spendMana(2);
			break;
			
			
		default:
			break;
		}
		
		
		if(jogador == 1){
			this.vidaHeroi1 = attacker.life;
			this.manaJogador1 = attacker.mana;
			this.vidaHeroi2 = target.life;
		} else {
			this.vidaHeroi2 = attacker.life;
			this.manaJogador2 = attacker.mana;
			this.vidaHeroi1 = target.life;
		}
		
		
		return;
	}
	
	private Carta comprarCarta(){
		if(this.jogador == 1){
			if(baralho1.getCartas().size() <= 0)
				throw new RuntimeException("Erro: Não há mais cartas no baralho para serem compradas.");
			Carta nova = baralho1.comprarCarta();
			mao.add(nova);
			nCartasHeroi1++;
			return (Carta) UnoptimizedDeepCopy.copy(nova);
		}
		else{
			if(baralho2.getCartas().size() <= 0)
				throw new RuntimeException("Erro: Não há mais cartas no baralho para serem compradas.");
			Carta nova = baralho2.comprarCarta();
			mao.add(nova);
			nCartasHeroi2++;
			return (Carta) UnoptimizedDeepCopy.copy(nova);
		}
	}	
}
