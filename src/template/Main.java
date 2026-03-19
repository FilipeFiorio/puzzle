package template;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.geom.Rectangle;
import br.com.davidbuzatto.jsge.image.Image;
import br.com.davidbuzatto.jsge.imgui.GuiButton;
import br.com.davidbuzatto.jsge.imgui.GuiSlider;
import br.com.davidbuzatto.jsge.sound.Music;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main extends EngineFrame {

    private int tamanho;

    private Peca[][] grade;
    private Peca[][] gradeBackup;
    private double tamanhoPeca;
    private Image imagemPeca;
    private Music somPeca;
    private Music somVitoria;

    //botoes
    private GuiButton botaoImagem;
    private GuiButton botaoComecar;
    private GuiButton botaoBackTrack;
    private GuiButton botaoCancelar;
    private GuiSlider sliderAnimacao;
    private GuiSlider sliderTamanho;

    //para mover as peças
    private Peca pecaMovendo;
    private double tempoAnimacao;
    private double tempoMaxAnimacao;
    private double xInicio;
    private double yInicio;
    private double xFim;
    private double yFim;
    private int linhaOrigem;
    private int colunaOrigem;
    private int linhaDestino;
    private int colunaDestino;

    private boolean animando;

    private EstadoJogo estadoJogo;

    private List<int[]> movimentosSolucao;
    private int indiceMovimento;
    private int contadorMovimentos;
    private static final int LIMITE_MOVIMENTOS = 8000;

    public Main() {

        super(600, 800, "Jogo Deslizante", 60, true);

    }

    @Override
    public void create() {

        useAsDependencyForIMGUI();

        tamanho = 3;
        grade = new Peca[tamanho][tamanho];
        tamanhoPeca = 600 / tamanho;
        imagemPeca = loadImage("resources/images/rio.png").resize(600, 600);
//        somPeca = loadMusic("resources/sfx/movimento.wav");
//        somVitoria = loadMusic("resources/sfx/vitoria.wav");

        //inicializando botoes
        botaoImagem = new GuiButton(15, 630, 180, 60, "Imagem");
        botaoComecar = new GuiButton(205, 630, 180, 60, "Comecar");
        botaoBackTrack = new GuiButton(395, 630, 180, 60, "Backtrack");
        botaoCancelar = new GuiButton(205, 700, 180, 60, "Cancelar");
        sliderAnimacao = new GuiSlider(
                new Rectangle(15, 730, 180, 30),
                10,
                1,
                20);

        sliderTamanho = new GuiSlider(
                new Rectangle(395, 730, 180, 30),
                3,
                2,
                10);

        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                grade[i][j] = new Peca(
                        j * tamanhoPeca,
                        i * tamanhoPeca,
                        tamanhoPeca,
                        i * tamanho + j,
                        imagemPeca
                );
            }
        }

        grade[tamanho - 1][tamanho - 1] = null;
        estadoJogo = EstadoJogo.Normal;
        gradeBackup = grade;

        tempoAnimacao = 0;
        tempoMaxAnimacao = 0.02;
        animando = false;

    }

    @Override
    public void update(double delta) {

        botaoImagem.update(delta);
        botaoComecar.update(delta);
        botaoBackTrack.update(delta);
        botaoCancelar.update(delta);

        sliderAnimacao.update(delta);
        sliderTamanho.update(delta);

        botaoImagem.setBackgroundColor(new Color(70, 130, 180));
        botaoComecar.setBackgroundColor(new Color(60, 179, 113));
        botaoCancelar.setBackgroundColor(new Color(255, 193, 7));
        botaoBackTrack.setBackgroundColor(new Color(220, 20, 60));

        sliderAnimacao.setTrackFillColor(new Color(70, 130, 180));
        sliderTamanho.setTrackFillColor(new Color(220, 20, 60));

        botaoBackTrack.setTextColor(WHITE);
        botaoComecar.setTextColor(WHITE);
        botaoImagem.setTextColor(WHITE);
        botaoCancelar.setTextColor(WHITE);

        tempoMaxAnimacao = 0.02 * sliderAnimacao.getValue();

        if (botaoCancelar.isMousePressed()) {

         
            if (pecaMovendo != null) {
                pecaMovendo.setPos(xFim, yFim);

                grade[linhaDestino][colunaDestino] = pecaMovendo;
                grade[linhaOrigem][colunaOrigem] = null;

                pecaMovendo = null;
            }

            grade = gradeBackup;
            contadorMovimentos = 0;

            if (estadoJogo == EstadoJogo.Resolvendo && movimentosSolucao != null) {
                movimentosSolucao.clear();
            }

            animando = false;

            reiniciarJogo();
            estadoJogo = EstadoJogo.Normal;
        }

        if (estadoJogo == estadoJogo.Normal) {

            botaoComecar.setEnabled(true);
            botaoImagem.setEnabled(true);
            botaoBackTrack.setEnabled(false);
            botaoCancelar.setEnabled(false);

            sliderAnimacao.setEnabled(true);
            sliderTamanho.setEnabled(true);

            int novoTamanho = (int) sliderTamanho.getValue();

            if (novoTamanho != tamanho) {
                tamanho = novoTamanho;
                reiniciarJogo();
            }

            if (botaoImagem.isMousePressed()) {
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter filtro = new FileNameExtensionFilter("Imagem", "png", "jpg");
                fileChooser.setFileFilter(filtro);

                int resultado = fileChooser.showOpenDialog(this);
                if (resultado == JFileChooser.APPROVE_OPTION) {
                    File arquivo = fileChooser.getSelectedFile();
                    imagemPeca = loadImage(arquivo.getAbsolutePath()).resize(600, 600);

                    for (int i = 0; i < tamanho; i++) {
                        for (int j = 0; j < tamanho; j++) {
                            if (grade[i][j] != null) {
                                grade[i][j].setImagem(imagemPeca);
                            }
                        }
                    }

                }

            } else if (botaoComecar.isMousePressed()) {
                embaralhar();
                estadoJogo = estadoJogo.Jogando;
            }

        } else if (estadoJogo == estadoJogo.Jogando) {

            botaoComecar.setEnabled(false);
            botaoImagem.setEnabled(false);
            botaoBackTrack.setEnabled(true);
            botaoCancelar.setEnabled(true);

            sliderTamanho.setEnabled(false);

            if (botaoBackTrack.isMousePressed()) {

                Set<String> visitados = new HashSet<>();

                try {
                    movimentosSolucao = new ArrayList<>();
                    indiceMovimento = 0;

                    Peca[][] backup = copiarGrade(grade);

                    contadorMovimentos = 0;
                    boolean resolveu = resolver(visitados, new ArrayList<>());

                    grade = backup;
                    recalcularPosicoes();

                    estadoJogo = EstadoJogo.Resolvendo;

                } catch (StackOverflowError e) {
                    System.out.println("Excede o limite de 8000 movimentos!!");

                }
            } else if (botaoComecar.isMousePressed()) {
                embaralhar();
            } else if (isMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
                for (int i = 0; i < tamanho; i++) {
                    for (int j = 0; j < tamanho; j++) {
                        if (grade[i][j] != null && grade[i][j].intercepta(getMouseX(), getMouseY())) {
                            moverPecaAnimado(i, j);
                        }
                    }
                }
            }

        } else if (estadoJogo == EstadoJogo.Resolvendo) {
            botaoBackTrack.setEnabled(false);
            botaoComecar.setEnabled(false);
            botaoImagem.setEnabled(false);
            botaoCancelar.setEnabled(true);
        }

        if (animando && pecaMovendo != null) {

            tempoAnimacao += delta;

            double t = tempoAnimacao / tempoMaxAnimacao;

            if (t > 1) {
                t = 1;
            }

            double x = xInicio + (xFim - xInicio) * t;
            double y = yInicio + (yFim - yInicio) * t;

            pecaMovendo.setPos(x, y);

            if (tempoAnimacao >= tempoMaxAnimacao) {

                animando = false;
                tempoAnimacao = 0;

                grade[linhaDestino][colunaDestino] = pecaMovendo;
                grade[linhaOrigem][colunaOrigem] = null;

                pecaMovendo.setPos(xFim, yFim);
                pecaMovendo = null;
            }

        }

        if (!animando && movimentosSolucao != null && indiceMovimento < movimentosSolucao.size()) {

            int[] mov = movimentosSolucao.get(indiceMovimento);

            moverPecaAnimado(mov[0], mov[1]);

            indiceMovimento++;
        }

        if (estaResolvido()) {
            estadoJogo = EstadoJogo.Normal;
        }

    }

    @Override
    public void draw() {

        clearBackground(new Color(30, 30, 40));

        botaoImagem.draw();
        botaoComecar.draw();
        botaoBackTrack.draw();
        botaoCancelar.draw();

        sliderAnimacao.draw();
        sliderTamanho.draw();

        drawText("Tempo Animação", 48.5, 720, 12, WHITE);
        drawText("Tamanho", 461, 720, 12, RAYWHITE);

        drawText(String.valueOf((int) sliderAnimacao.getValue()), 97.5, 770, 12, RAYWHITE);
        drawText(String.valueOf((int) sliderTamanho.getValue()), 482.5, 770, 12, RAYWHITE);

        //System.out.println(measureText("Tamanho", 15));
        drawLine(0, 600, 600, 600, BLACK);

        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                if (grade[i][j] != null) {
                    grade[i][j].desenhar(this, tamanho);
                }
            }
        }

        if (pecaMovendo != null) {
            pecaMovendo.desenhar(this, tamanho);
        }

        if (estadoJogo == EstadoJogo.Resolvendo) {
            int movimentosRestantes = movimentosSolucao.size() - indiceMovimento;
            drawText("Movimentos Restantes: " + String.valueOf(movimentosRestantes), 15, 610, 20, RED);
        }

    }

    private void moverPeca(int lin, int col) {

        System.out.println("Mover Peca");
        int[] vizLin = {-1, 0, 1, 0};
        int[] vizCol = {0, 1, 0, -1};

        int linDest = -1;
        int colDest = -1;

        for (int i = 0; i < 4; i++) {
            int linAt = lin + vizLin[i];
            int colAt = col + vizCol[i];

            if (linAt >= 0 && linAt < tamanho && colAt >= 0 && colAt < tamanho) {
                if (grade[linAt][colAt] == null) {
                    linDest = linAt;
                    colDest = colAt;
                    break;
                }
            }
        }

        if (linDest != -1) {
            grade[linDest][colDest] = grade[lin][col];
            grade[lin][col] = null;
            recalcularPosicoes();
            //somPeca.play();
        }

    }

    private void moverPecaAnimado(int lin, int col) {

        int[] vizLin = {-1, 0, 1, 0};
        int[] vizCol = {0, 1, 0, -1};

        int linDest = -1;
        int colDest = -1;

        for (int i = 0; i < 4; i++) {
            int linAt = lin + vizLin[i];
            int colAt = col + vizCol[i];

            if (linAt >= 0 && linAt < tamanho && colAt >= 0 && colAt < tamanho) {
                if (grade[linAt][colAt] == null) {
                    linDest = linAt;
                    colDest = colAt;
                    break;
                }
            }
        }

        if (linDest != -1) {

            Peca p = grade[lin][col];

            linhaOrigem = lin;
            colunaOrigem = col;
            linhaDestino = linDest;
            colunaDestino = colDest;

            xInicio = col * tamanhoPeca;
            yInicio = lin * tamanhoPeca;

            xFim = colDest * tamanhoPeca;
            yFim = linDest * tamanhoPeca;

            pecaMovendo = p;
            animando = true;
        }
    }

    private void recalcularPosicoes() {
        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                if (grade[i][j] != null) {
                    grade[i][j].setPos(j * tamanhoPeca, i * tamanhoPeca);
                }
            }
        }
    }

    private void embaralhar() {

        int movimentos = 15;

        int linVazio = tamanho - 1;
        int colVazio = tamanho - 1;

        int[] vizLin = {-1, 0, 1, 0};
        int[] vizCol = {0, 1, 0, -1};

        for (int k = 0; k < movimentos; k++) {

            while (true) {
                int dir = (int) (Math.random() * 4);

                int lin = linVazio + vizLin[dir];
                int col = colVazio + vizCol[dir];

                if (lin >= 0 && lin < tamanho && col >= 0 && col < tamanho) {

                    Peca temp = grade[lin][col];
                    grade[lin][col] = null;
                    grade[linVazio][colVazio] = temp;

                    linVazio = lin;
                    colVazio = col;
                    break;
                }
            }
        }

        int alvoLin = tamanho - 1;
        int alvoCol = tamanho - 1;

        while (linVazio < alvoLin) {
            Peca temp = grade[linVazio + 1][colVazio];
            grade[linVazio + 1][colVazio] = null;
            grade[linVazio][colVazio] = temp;
            linVazio++;
        }

        while (linVazio > alvoLin) {
            Peca temp = grade[linVazio - 1][colVazio];
            grade[linVazio - 1][colVazio] = null;
            grade[linVazio][colVazio] = temp;
            linVazio--;
        }

        while (colVazio < alvoCol) {
            Peca temp = grade[linVazio][colVazio + 1];
            grade[linVazio][colVazio + 1] = null;
            grade[linVazio][colVazio] = temp;
            colVazio++;
        }

        while (colVazio > alvoCol) {
            Peca temp = grade[linVazio][colVazio - 1];
            grade[linVazio][colVazio - 1] = null;
            grade[linVazio][colVazio] = temp;
            colVazio--;
        }

        recalcularPosicoes();
    }

    private String estadoAtual() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                if (grade[i][j] == null) {
                    sb.append("X");
                } else {
                    sb.append(grade[i][j].getValor());
                }
            }
        }

        return sb.toString();
    }

    private boolean estaResolvido() {
        int valor = 0;

        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                if (i == tamanho - 1 && j == tamanho - 1) {
                    return grade[i][j] == null;
                }

                if (grade[i][j] == null || grade[i][j].getValor() != valor) {
                    return false;
                }

                valor++;
            }
        }

        estadoJogo = EstadoJogo.Normal;
        return true;
    }

    private boolean resolver(Set<String> visitados, List<int[]> caminho) {

        if (contadorMovimentos > LIMITE_MOVIMENTOS) {
            return false;
        }

        if (estaResolvido()) {
            movimentosSolucao = new java.util.ArrayList<>(caminho);
            return true;
        }

        String estado = estadoAtual();

        if (visitados.contains(estado)) {
            return false;
        }

        visitados.add(estado);

        int[] vizLin = {-1, 0, 1, 0};
        int[] vizCol = {0, 1, 0, -1};

        int linVazio = -1;
        int colVazio = -1;

        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                if (grade[i][j] == null) {
                    linVazio = i;
                    colVazio = j;
                }
            }
        }

        for (int i = 0; i < 4; i++) {

            int lin = linVazio + vizLin[i];
            int col = colVazio + vizCol[i];

            if (lin >= 0 && lin < tamanho && col >= 0 && col < tamanho) {

                trocar(linVazio, colVazio, lin, col);
                caminho.add(new int[]{lin, col}); // salva movimento

                if (resolver(visitados, caminho)) {
                    return true;
                }

                caminho.remove(caminho.size() - 1);
                trocar(linVazio, colVazio, lin, col);
            }
        }

        return false;
    }

    private void trocar(int l1, int c1, int l2, int c2) {
        Peca temp = grade[l1][c1];
        grade[l1][c1] = grade[l2][c2];
        grade[l2][c2] = temp;
    }

    private Peca[][] copiarGrade(Peca[][] original) {
        Peca[][] copia = new Peca[tamanho][tamanho];

        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                copia[i][j] = original[i][j];
            }
        }

        return copia;
    }

    private void reiniciarJogo() {

        grade = new Peca[tamanho][tamanho];
        tamanhoPeca = 600.0 / tamanho;

        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                grade[i][j] = new Peca(
                        j * tamanhoPeca,
                        i * tamanhoPeca,
                        tamanhoPeca,
                        i * tamanho + j,
                        imagemPeca
                );
            }
        }

        grade[tamanho - 1][tamanho - 1] = null;
        recalcularPosicoes();
    }

    public static void main(String[] args) {
        new Main();
    }

    private enum EstadoJogo {
        Normal,
        Jogando,
        Resolvendo;
    }

}
