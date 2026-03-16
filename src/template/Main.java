package template;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.image.Image;
import br.com.davidbuzatto.jsge.imgui.GuiButton;
import br.com.davidbuzatto.jsge.sound.Music;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Modelo de projeto básico da JSGE.
 *
 * JSGE basic project template.
 *
 * @author Prof. Dr. David Buzatto
 */
public class Main extends EngineFrame {

    private static final int TAMANHO = 3;

    private Peca[][] grade;
    private double tamanhoPeca;
    private Image imagemPeca;
    private Music somPeca;
    private Music somVitoria;

    //botoes
    private GuiButton botaoImagem;

    private EstadoJogo estadoJogo;

    public Main() {

        super(600, 800, "Jogo Deslizante", 60, true);

    }

    @Override
    public void create() {

        useAsDependencyForIMGUI();

        grade = new Peca[TAMANHO][TAMANHO];
        tamanhoPeca = 600 / TAMANHO;
        imagemPeca = loadImage("resources/images/rio.png").resize(600, 600);
        somPeca = loadMusic("resources/sfx/movimento.wav");
        somVitoria = loadMusic("resources/sfx/vitoria.wav");

        //inicializando botoes
        botaoImagem = new GuiButton(10, 670, 180, 60, "Imagem");

        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                grade[i][j] = new Peca(
                        j * tamanhoPeca,
                        i * tamanhoPeca,
                        tamanhoPeca,
                        i * TAMANHO + j,
                        imagemPeca
                );
            }
        }

        grade[TAMANHO - 1][TAMANHO - 1] = null;
        estadoJogo = EstadoJogo.Normal;
    }

    @Override
    public void update(double delta) {

        botaoImagem.update(delta);

        //if (estadoJogo == estadoJogo.Jogando) {
        if (isMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
            for (int i = 0; i < TAMANHO; i++) {
                for (int j = 0; j < TAMANHO; j++) {
                    if (grade[i][j] != null && grade[i][j].intercepta(getMouseX(), getMouseY()) && !somPeca.isPlaying()) {
                        moverPeca(i, j);
                    }
                }
            }
        }
        //}

        if (botaoImagem.isMousePressed()) {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filtro = new FileNameExtensionFilter("Imagem","png", "jpg");
            fileChooser.setFileFilter(filtro);

            int resultado = fileChooser.showOpenDialog(this);
            if (resultado == JFileChooser.APPROVE_OPTION) {
                File arquivo = fileChooser.getSelectedFile();
                imagemPeca = loadImage(arquivo.getAbsolutePath()).resize(600, 600);
                
                for(int i = 0; i < TAMANHO; i++) {
                    for(int j = 0; j < TAMANHO; j++) {
                        if(grade[i][j] != null) {
                            grade[i][j].setImagem(imagemPeca);        
                        }
                    }
                }
                System.out.println("Caminho: " + arquivo.getAbsolutePath());
            }

        }
    }

    @Override
    public void draw() {

        clearBackground(WHITE);

        botaoImagem.draw();

        drawLine(0, 600, 600, 600, BLACK);

        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                if (grade[i][j] != null) {
                    grade[i][j].desenhar(this, TAMANHO);
                }
            }
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

            if (linAt >= 0 && linAt < TAMANHO && colAt >= 0 && colAt < TAMANHO) {
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
            somPeca.play();
        }

    }

    private void recalcularPosicoes() {
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                if (grade[i][j] != null) {
                    grade[i][j].setPos(j * tamanhoPeca, i * tamanhoPeca);
                }
            }
        }
    }

    private void embaralhar() {

    }

    public static void main(String[] args) {
        new Main();
    }

    private enum EstadoJogo {
        Normal,
        Jogando,
        MovendoPeca,
    }

}
