# LaMa

Este projeto consiste em um jogo de cartas composto por **la**caios e **ma**gias, cujo desenvolvimento tem como finalidade a familiarização dos conceitos da Programação Orientada a Objetos.

### Regras do Jogo

1. Cada jogador inicia com um Herói com 30 pontos de vida. Vence o jogador que reduzir a vida do
Herói do oponente a zero.
2. O baralho possui trinta cartas, sendo 22 Lacaios e 8 Magias.
3. O primeiro jogador inicia com 3 cartas, enquanto o segundo jogador inicia o jogo com 4 cartas.
4. Todo o início de turno cada jogador compra uma carta do baralho, de maneira aleatória.

5. No turno i, cada jogador possui min(i, 10) de mana para ser utilizado em uso de cartas ou poder
heroico, não podendo ultrapassar este valor. O limite de mana é acrescido até o décimo turno e
então não aumenta mais. Exceção: O segundo jogador possui dois de mana no primeiro turno ao
invés de um de mana.
6. Existem dois tipos de carta: Lacaios e Magias.
• Lacaios são baixados à mesa e não podem atacar no turno em que são baixados, apenas no
turno seguinte (se ainda estiverem vivos!).
• Magias possuem efeitos imediatos, ao serem utilizadas causam dano na vida de um único alvo
inimigo ou em todos os alvos inimigos (efeito em área). As magias de alvo podem ter como
alvo o Herói do oponente ou um dos Lacaios na mesa do oponente. As magias de efeito em
área causam dano nos Lacaios do oponente e no Herói do oponente.
7. Não é possível ter mais de 7 lacaios em campo.
8. Um lacaio só pode atacar no máximo uma vez por turno, assim como o poder heroico também só
pode ser utilizado no máximo uma vez por turno.
9. O jogador pode utilizar, uma vez por turno, o poder heroico de seu Herói ao custo de duas unidades
de mana. O poder heroico faz o Herói atacar um alvo qualquer com um de dano. Se o alvo do poder
heroico for um Lacaio, o Herói que atacou receberá de dano o ataque do Lacaio alvo.
10. Cada Lacaio pode escolher atacar um alvo por turno. Os possíveis alvos são: o Herói do oponente
e os Lacaios em mesa do oponente. Se o Lacaio x atacar o Lacaio y, o Lacaio y tem sua vida
diminuída pelo poder de ataque do Lacaio x, e o Lacaio x tem sua vida diminuída pelo poder de
ataque do Lacaio y. Se a vida de um Lacaio chegar a zero, ele morre e é retirado da mesa.
11. Se um Lacaio atacar um Herói, a vida do Herói é reduzida pelo poder de ataque do Lacaio mas o
Lacaio não sofre nenhum tipo de dano.
12. O jogador pode possuir até dez cartas na mão. Se já possuir dez cartas ao início de seu turno, a
nova carta que seria comprada é descartada.
13. Quando o jogador fica sem cartas para comprar do baralho chamamos esse estado de jogo de fadiga.
A fadiga inicia quando em um dado turno não for possível a um jogador comprar cartas porque o
baralho já está esgotado, então o jogador recebe um de dano no início daquele turno. No próximo
início de turno receberá dois de dano, depois três, e assim por diante.
14. Para o Trabalho 2, os lacaios podem possuir efeitos especiais de investida, ataque duplo ou
provocar. (Seção 5 do enunciado do Trabalho 2).
