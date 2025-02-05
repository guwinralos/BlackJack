import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class BlackJack {
    private class Carta {
        String valor;
        String tipo;

        Carta(String valor, String tipo) {
            this.valor = valor;
            this.tipo = tipo;
        }

        public String toString() {
            return valor + "-" + tipo;
        }

        public int getValor() {
            if ("AJQK".contains(valor)) { 
                if (valor.equals("A")) {
                    return 11;
                }
                return 10;
            }
            return Integer.parseInt(valor); 
        }

        public boolean isAs() {
            return valor.equals("A");
        }

        public String getImagePath() {
            return "./cards/" + toString() + ".png";
        }
    }

    ArrayList<Carta> baralho;
    Random aleatorio = new Random(); 

   
    Carta cartaOculta;
    ArrayList<Carta> maoDealer;
    int somaDealer;
    int contagemAsDealer;

    
    ArrayList<Carta> maoJogador;
    int somaJogador;
    int contagemAsJogador;

   
    int larguraTabuleiro = 600;
    int alturaTabuleiro = larguraTabuleiro;

    int larguraCarta = 110; // proporção deve ser 1/1.4
    int alturaCarta = 154;

    JFrame quadro = new JFrame("Black Jack");
    JPanel painelJogo = new JPanel() {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            try {
                
                Image imgCartaOculta = new ImageIcon(getClass().getResource("./cards/BACK.png")).getImage();
                if (!botaoSair.isEnabled()) {
                    imgCartaOculta = new ImageIcon(getClass().getResource(cartaOculta.getImagePath())).getImage();
                }
                g.drawImage(imgCartaOculta, 20, 20, larguraCarta, alturaCarta, null);

                
                for (int i = 0; i < maoDealer.size(); i++) {
                    Carta carta = maoDealer.get(i);
                    Image imgCarta = new ImageIcon(getClass().getResource(carta.getImagePath())).getImage();
                    g.drawImage(imgCarta, larguraCarta + 25 + (larguraCarta + 5) * i, 20, larguraCarta, alturaCarta, null);
                }

               
                for (int i = 0; i < maoJogador.size(); i++) {
                    Carta carta = maoJogador.get(i);
                    Image imgCarta = new ImageIcon(getClass().getResource(carta.getImagePath())).getImage();
                    g.drawImage(imgCarta, 20 + (larguraCarta + 5) * i, 320, larguraCarta, alturaCarta, null);
                }

                if (!botaoSair.isEnabled()) {
                    somaDealer = reduzirAsDealer();
                    somaJogador = reduzirAsJogador();
                    String mensagem = "";
                    if (somaJogador > 21) {
                        mensagem = "Você Perdeu!";
                        saldo -= aposta; 
                    } else if (somaDealer > 21) {
                        mensagem = "Você Ganhou!";
                        saldo += aposta; 
                    } else if (somaJogador == somaDealer) {
                        mensagem = "Empate!";
                    } else if (somaJogador > somaDealer) {
                        mensagem = "Você Ganhou!";
                        saldo += aposta; 
                    } else if (somaJogador < somaDealer) {
                        mensagem = "Você Perdeu!";
                        saldo -= aposta; 
                    }

                    g.setFont(new Font("Arial", Font.PLAIN, 30));
                    g.setColor(Color.white);
                    g.drawString(mensagem, 220, 250);
                    g.drawString("Saldo: " + saldo, 220, 300); 

                   
                    if (saldo <= 0) {
                        JOptionPane.showMessageDialog(quadro, "Você não tem mais saldo. O jogo será encerrado.");
                        System.exit(0); 
                    }

                    
                    Timer timer = new Timer(2000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            iniciarJogo(); 
                            botaoSair.setEnabled(true); 
                            botaoPedir.setEnabled(true); 
                            painelJogo.repaint(); 
                        }
                    });
                    timer.setRepeats(false); 
                    timer.start(); //timer
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    JPanel painelBotoes = new JPanel();
    JButton botaoPedir = new JButton("Pedir");
    JButton botaoSair = new JButton("Sair");
    JTextField campoAposta = new JTextField(10); //entrada 
    int saldo = 100; 
    int aposta = 0; 

    BlackJack() {
        iniciarJogo();

        quadro.setVisible(true);
        quadro.setSize(larguraTabuleiro, alturaTabuleiro);
        quadro.setLocationRelativeTo(null);
        quadro.setResizable(false);
        quadro.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        painelJogo.setLayout(new BorderLayout());
        painelJogo.setBackground(new Color(53, 101, 77));
        quadro.add(painelJogo);

        painelBotoes.add(new JLabel("Aposta:"));
        painelBotoes.add(campoAposta); 
        botaoPedir.setFocusable(false);
        painelBotoes.add(botaoPedir);
        botaoSair.setFocusable(false);
        painelBotoes.add(botaoSair);
        quadro.add(painelBotoes, BorderLayout.SOUTH);

        botaoPedir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    aposta = Integer.parseInt(campoAposta.getText());
                    if (aposta > saldo) {
                        JOptionPane.showMessageDialog(quadro, "Aposta não pode ser maior que o saldo!");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(quadro, "Por favor, insira um valor válido para a aposta.");
                    return;
                }

                Carta carta = baralho.remove(baralho.size() - 1);
                somaJogador += carta.getValor();
                contagemAsJogador += carta.isAs() ? 1 : 0;
                maoJogador.add(carta);
                if (reduzirAsJogador() > 21) { // A + 2 + J --> 1 + 2 + J
                    botaoPedir.setEnabled(false);
                }
                painelJogo.repaint();
            }
        });

        botaoSair.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                botaoPedir.setEnabled(false);
                botaoSair.setEnabled(false);

                while (somaDealer < 17) {
                    Carta carta = baralho.remove(baralho.size() - 1);
                    somaDealer += carta.getValor();
                    contagemAsDealer += carta.isAs() ? 1 : 0;
                    maoDealer.add(carta);
                }
                painelJogo.repaint();
            }
        });

        painelJogo.repaint();
    }

    public void iniciarJogo() {
        // baralho
        construirBaralho();
        embaralharBaralho();

        // dealer
        maoDealer = new ArrayList<Carta>();
        somaDealer = 0;
        contagemAsDealer = 0;

        cartaOculta = baralho.remove(baralho.size() - 1);
        somaDealer += cartaOculta.getValor();
        contagemAsDealer += cartaOculta.isAs() ? 1 : 0;

        Carta carta = baralho.remove(baralho.size() - 1);
        somaDealer += carta.getValor();
        contagemAsDealer += carta.isAs() ? 1 : 0;
        maoDealer.add(carta);

        // jogador
        maoJogador = new ArrayList<Carta>();
        somaJogador = 0;
        contagemAsJogador = 0;

        for (int i = 0; i < 2; i++) {
            carta = baralho.remove(baralho.size() - 1);
            somaJogador += carta.getValor();
            contagemAsJogador += carta.isAs() ? 1 : 0;
            maoJogador.add(carta);
        }

        painelJogo.repaint();
    }

    public void construirBaralho() {
        baralho = new ArrayList<Carta>();
        String[] valores = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] tipos = {"C", "D", "H", "S"};

        for (String tipo : tipos) {
            for (String valor : valores) {
                Carta carta = new Carta(valor, tipo);
                baralho.add(carta);
            }
        }
    }

    public void embaralharBaralho() {
        for (int i = 0; i < baralho.size(); i++) {
            int j = aleatorio.nextInt(baralho.size());
            Carta cartaAtual = baralho.get(i);
            Carta cartaAleatoria = baralho.get(j);
            baralho.set(i, cartaAleatoria);
            baralho.set(j, cartaAtual);
        }
    }

    public int reduzirAsJogador() {
        while (somaJogador > 21 && contagemAsJogador > 0) {
            somaJogador -= 10;
            contagemAsJogador -= 1;
        }
        return somaJogador;
    }

    public int reduzirAsDealer() {
        while (somaDealer > 21 && contagemAsDealer > 0) {
            somaDealer -= 10;
            contagemAsDealer -= 1;
        }
        return somaDealer;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new BlackJack();
            }
        });
    }
}
