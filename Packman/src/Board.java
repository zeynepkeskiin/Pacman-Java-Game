import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Board extends JPanel implements ActionListener {

    private Dimension d;  //boyut
    private final Font smallFont = new Font("Helvetica", Font.BOLD, 14); //Score yazısında font ve boyut

    private Image ii;
    private final Color dotColor = new Color(192, 192, 0); // noktaların sarı olması
    private Color mazeColor; // labirent rengi

    private boolean inGame = false; // oyunda
    private boolean dying = false; //ölme

    private final int BLOCK_SIZE = 24; // pacmanin yeminin bir karesi
    private final int N_BLOCKS = 15; // 15 e 15 sutün ve satırımız
    private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE; // ikisinin çarpımı ekran boyutunu veriyor.çarpmazsa ekran gelmiyor. çaprazlama olarak tüm
    // ekran
    private final int PAC_ANIM_DELAY = 2; //animasyou yavaşlatan sabit
    private final int PACMAN_ANIM_COUNT = 4;
    private final int MAX_GHOSTS = 12;
    private final int PACMAN_SPEED = 6; //pacman hızı

    private int pacAnimCount = PAC_ANIM_DELAY;  //pacmani yavaşlatma amaçlı
    private int pacAnimDir = 1;
    private int pacmanAnimPos = 0;
    private int N_GHOSTS = 6; // 6 tane hayalet
    private int pacsLeft, score; // pacman canı
    private int[] dx, dy;  //koordinatlar
    private int[] ghost_x, ghost_y, ghost_dx, ghost_dy, ghostSpeed; // hayaletlerin gidiş yönleri ve hızı

    private Image ghost; // hayalet resmi
    private Image pacman1, pacman2up, pacman2left, pacman2right, pacman2down; // pacman yukarı aşağı sağa sola
    private Image pacman3up, pacman3down, pacman3left, pacman3right; // pacmani oynatırken ağzının açılıp kapanması
    private Image pacman4up, pacman4down, pacman4left, pacman4right;

    private int pacman_x, pacman_y, pacmand_x, pacmand_y; // pacman gittiği yönler
    private int req_dx, req_dy, view_dx, view_dy; //  reqler tuşların hareketi, görünüm

    private final short levelData[] = {              //noktaların koordinatı 0 olanlar bloklarımız 16 dan küçükler bloklarımız
            19, 26, 26, 26, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
            21, 0, 0, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            21, 0, 0, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            21, 0, 0, 0, 17, 16, 16, 24, 16, 16, 16, 16, 16, 16, 20,
            17, 18, 18, 18, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 16, 24, 20,
            25, 16, 16, 16, 24, 24, 28, 0, 25, 24, 24, 16, 20, 0, 21,
            1, 17, 16, 20, 0, 0, 0, 0, 0, 0, 0, 17, 20, 0, 21,
            1, 17, 16, 16, 18, 18, 22, 0, 19, 18, 18, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 16, 18, 16, 16, 16, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0, 21,
            1, 25, 24, 24, 24, 24, 24, 24, 24, 24, 16, 16, 16, 18, 20,
            9, 8, 8, 8, 8, 8, 8, 8, 8, 8, 25, 24, 24, 24, 28
    };

    private final int validSpeeds[] = {1, 2, 3, 4, 6, 8}; //geçerli hızlar
    private final int maxSpeed = 6; //max hız

    private int currentSpeed = 3; //geçeri hız
    private short[] screenData;
    private Timer timer;

    public Board() {

        loadImages(); // foto çağırıyoruz
        initVariables(); // değişkenleri çağırıyoruz
        initBoard();
    }

    private void initBoard() {

        addKeyListener(new TAdapter()); //klavyedeki hareketleri algılama

        setFocusable(true); // oyunu başlatmamıza yarıyor girdilerin odağını ayarlar klavye ve mouse

        setBackground(Color.black); // arka plan siyah
    }

    private void initVariables() {

        screenData = new short[N_BLOCKS * N_BLOCKS];
        mazeColor = new Color(5, 100, 5); // labirent  bloklarının rengi
        d = new Dimension(400, 400);
        ghost_x = new int[MAX_GHOSTS];
        ghost_dx = new int[MAX_GHOSTS];
        ghost_y = new int[MAX_GHOSTS];
        ghost_dy = new int[MAX_GHOSTS];
        ghostSpeed = new int[MAX_GHOSTS];
        dx = new int[4];
        dy = new int[4]; //hayaletlerin ilerleyebileceği kısım

        timer = new Timer(40, this); // pacman ve hayalet süreleri
        timer.start();
    }

    @Override
    public void addNotify() { //bildirim ekleme
        super.addNotify();

        initGame();// oyunu başlatıyoruz
    }

    private void doAnim() {
        pacAnimCount--;

        if (pacAnimCount <= 0) {
            pacAnimCount = PAC_ANIM_DELAY;
            pacmanAnimPos = pacmanAnimPos + pacAnimDir; //pacanimpos ağzı açıp kapamak için 3 foto var

            if (pacmanAnimPos == (PACMAN_ANIM_COUNT - 1) || pacmanAnimPos == 0) {
                pacAnimDir = -pacAnimDir;             // pacmani yavaşlatma amacı ile yazıldı yoksa pacmanin ağzı çok hızlı açılıp kapanırdı.


            }
        }
    }

    private void playGame(Graphics2D g2d) { // oyunu oynatma metodumuz

        if (dying) {

            death(); // eğer dying çalışırsa yanarsak öldü.

        } else {

            movePacman(); // eğer yanmazsa pacman hareket etsin
            drawPacman(g2d); //çiz
            moveGhosts(g2d); // hayaletleri hareket ettir
            checkMaze(); // labirenti kontrol etmek
        }
    }

    private void showIntroScreen(Graphics2D g2d) { // giriş ekranını göster

        g2d.setColor(new Color(0, 32, 48)); // ekran rengimiz
        g2d.fillRect(50, SCREEN_SIZE / 2 - 30, SCREEN_SIZE - 100, 50); //kare çizdir İÇİNDEKİ BAŞLATMA YERİ iç kısım
        g2d.setColor(Color.white); //beyaza boya OYUNU BAŞLATMA YERİ çerçeve
        g2d.drawRect(50, SCREEN_SIZE / 2 - 30, SCREEN_SIZE - 100, 50);

        String s = "Press s to start.";
        Font small = new Font("Helvetica", Font.BOLD, 14); // press s to start kısmı.
        FontMetrics metr = this.getFontMetrics(small);

        g2d.setColor(Color.white);
        g2d.setFont(small);
        g2d.drawString(s, (SCREEN_SIZE - metr.stringWidth(s)) / 2, SCREEN_SIZE / 2); // yazıyı ortalmak için
    }

    private void drawScore(Graphics2D g) {

        int i;
        String s;

        g.setFont(smallFont);
        g.setColor(new Color(96, 128, 255));
        s = "Score: " + score;
        g.drawString(s, SCREEN_SIZE / 2 + 96, SCREEN_SIZE + 16);// score yazısının yeri

        for (i = 0; i < pacsLeft; i++) {
            g.drawImage(pacman3left, i * 28 + 1, SCREEN_SIZE + 1, this); //can kısmını üst üste koymaması için canların yeri pacman3 ağzı açık

        }
    }
    private void checkMaze() { // labirenti kontrol et

        short i = 0;
        boolean finished = true;

        while (i < N_BLOCKS * N_BLOCKS && finished) {// pacmanin yemesi için herhangi bir nokta olup olmadığını kontrol ediyoruz

            if ((screenData[i] & 48) != 0) { // tüm puanları yani yemleri yediğimizde bir sonraki levele geçeriz
                finished = false;


            }

            i++;
        }


        if (finished) {

            score += 50; // eğer bitirsen score 50 puan ekleniyor

            if (N_GHOSTS < MAX_GHOSTS) {
                N_GHOSTS++; // level geçtikçe hayalet artıyor
            }

            if (currentSpeed < maxSpeed) { // bölüm geçtikçe hayaletlerin hızı artıyor en fazla 6 olabilir hızlar
                currentSpeed++;
            }

            initLevel(); // başlangıç leveli
        }

    }

    private void death() {

        pacsLeft--;

        if (pacsLeft == 0) {
            inGame = false; // canlar 0 lanınca oyundan çıkıyoruz
        }

        continueLevel(); // leveli tekrarlıyoruz.
    }

    private void moveGhosts(Graphics2D g2d) { //hayaletlerin hareket metodu

        short i;
        int pos;
        int count;

        for (i = 0; i < N_GHOSTS; i++) {  // hayalet sayılarımız
            if (ghost_x[i] % BLOCK_SIZE == 0 && ghost_y[i] % BLOCK_SIZE == 0) {  //bloklarımız 0 ise hayaletler sağa sola gidemiyor
                pos = ghost_x[i] / BLOCK_SIZE + N_BLOCKS * (int) (ghost_y[i] / BLOCK_SIZE); // hayaletlerin  konumu 225 teorik konum var duvarlardan geçemez

                count = 0;


                if ((screenData[pos] & 1) == 0 && ghost_dx[i] != 1) { // bir kare var 1 sol 2 yukarı 4 sağ 8 aşağı 1 sayesinde hayaletler sola gidebilir
                    dx[count] = -1; // burası çalışmazsa yine hayaletler sağa gidemez
                    dy[count] = 0; // x -1 y 0 ise sola git demek
                    count++;// Hayalet bir tünele girerse tünelden çıkana kadar aynı yönde devam edecektir. Hayaletlerin taşınması kısmen rastgeledir.
                    // Bu rasgeleliği uzun tünellerde uygulamıyoruz çünkü hayalet orada sıkışıp kalabilir.
                }

                if ((screenData[pos] & 2) == 0 && ghost_dy[i] != 1) { // 2 yukarı git demek
                    dx[count] = 0; //x in 0 y nin -1 olması lazım. koordinat düzlemine göre
                    dy[count] = -1;
                    count++;
                }

                if ((screenData[pos] & 4) == 0 && ghost_dx[i] != -1) { // 4 sola git demek
                    dx[count] = 1; // sola gidebilmek için x im 1  y 0 olur
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 8) == 0 && ghost_dy[i] != -1) { // 8 aşağı git demek
                    dx[count] = 0; //aşağı gidebilmek için x 0 y 1 olmalıdır.
                    dy[count] = 1;
                    count++;
                }

                if (count == 0) {


                    if ((screenData[pos] & 15) == 15) {
                        ghost_dx[i] = 0;
                        ghost_dy[i] = 0; // burası hayaletlerin duvar dışına çıkmasını engeller.

                    } else {
                        ghost_dx[i] = -ghost_dx[i];
                        ghost_dy[i] = -ghost_dy[i];  // else çalışırsa hayaletler bir adım gidip sonra tekrar gelicektir.
                    }

                } else {

                    count = (int) (Math.random() * count); //hayaletlerin rastgele gezmesini sağlıyor.

                    if (count >3) {    // count 3 den büyük değer alamıyor. countu bu yüzden 3 e eşitleriz.
                        count = 3;   // bloklara değmediği sürece  hayaletler , 3 sağ 3 sol 3 yukarı 3 aşağıdan fazla adım atamazlar daha az adım atabilirler.
                    }
                    ghost_dx[i] = dx[count];
                    ghost_dy[i] = dy[count];
                }


            }

            ghost_x[i] = ghost_x[i] + (ghost_dx[i] * ghostSpeed[i]);
            ghost_y[i] = ghost_y[i] + (ghost_dy[i] * ghostSpeed[i]);
            drawGhost(g2d, ghost_x[i] + 1, ghost_y[i] + 1); //hayaletlerimizin konumu ve hızını ayarlıyoruz ve çizdiriyoruz

            if (pacman_x > (ghost_x[i] - 12) && pacman_x < (ghost_x[i] + 12)  // pacman ve hayalet arasındaki 12 piksel uzaklık 100 yaparsak yakınına gelip değmeden ölüyor
                    && pacman_y > (ghost_y[i] - 12) && pacman_y < (ghost_y[i] + 12)
                    && inGame) {

                dying = true; //Hayaletler ve Pacman arasında bir çarpışma olursa Pacman ölür.
            }
        }
    }

    private void drawGhost(Graphics2D g2d, int x, int y) {

        g2d.drawImage(ghost, x, y, this);    //hayaletlerimizi çizdiriyoruz
    }

    private void movePacman() {  // pacmani hareket ettir

        int pos;
        short ch;

        if (req_dx == -pacmand_x && req_dy == -pacmand_y) {  //
            pacmand_x = req_dx;
            pacmand_y = req_dy;
            view_dx = pacmand_x; //pacamnin yeni görünümü
            view_dy = pacmand_y;
        }

        if (pacman_x % BLOCK_SIZE == 0 && pacman_y % BLOCK_SIZE == 0) { //bloklarımız 0 ise pacman sağa sola gidemiyor
            pos = pacman_x / BLOCK_SIZE + N_BLOCKS * (int) (pacman_y / BLOCK_SIZE);  // pacman  konumu 225 teorik konum var duvarlardan geçemez
            ch = screenData[pos];

            if ((ch & 16) != 0) {
                screenData[pos] = (short) (ch & 15); //yukarıda yazdığımız yemlerin koordinatları  yedikçe o koordinatları score 1 1 ekler.
                score++;

            }//Pacman bir noktaya sahip bir konuma hareket ederse, onu labirentten çıkarır ve puan değerini artırırız.

            if (req_dx != 0 || req_dy != 0) {
                if (!((req_dx == -1 && req_dy == 0 && (ch & 1) != 0) //sağ
                        || (req_dx == 1 && req_dy == 0 && (ch & 4) != 0) // sol
                        || (req_dx == 0 && req_dy == -1 && (ch & 2) != 0) // yukarı
                        || (req_dx == 0 && req_dy == 1 && (ch & 8) != 0))) { // aşağı
                    pacmand_x = req_dx; // sağ sol tuşu pacmanin x koordinatlarının sağ sol tuşlarından alır.
                    pacmand_y = req_dy; // aşağı yukarı tuşu pacmanin x koordinatlarının aşağı yukarı tuşlarından alır.
                    view_dx = pacmand_x; //view kısmı pacmanin x koordinat görünümlerini sağ sol hareketlerinden alır atar
                    view_dy = pacmand_y;//view kısmı pacmanin y koordinat görünümlerini yukarı aşağı hareketlerinden alır atar
                }
            }

            // Check for standstill
            if ((pacmand_x == -1 && pacmand_y == 0 && (ch & 1) != 0)//sağ
                    || (pacmand_x == 1 && pacmand_y == 0 && (ch & 4) != 0)//sol
                    || (pacmand_x == 0 && pacmand_y == -1 && (ch & 2) != 0)//yukarı
                    || (pacmand_x == 0 && pacmand_y == 1 && (ch & 8) != 0)) {//aşağı
                pacmand_x = 0;
                pacmand_y = 0;         //pacman, şu anki yönünü ilerletemezse durur.
            }
        }
        pacman_x = pacman_x + PACMAN_SPEED * pacmand_x; // pacmanimizi hareket ettirme kodlarımız
        pacman_y = pacman_y + PACMAN_SPEED * pacmand_y;
    }

    private void drawPacman(Graphics2D g2d) {

        if (view_dx == -1) {     // -1 ise sola
            drawPacnanLeft(g2d);
        } else if (view_dx == 1) { // ximiz 1 ise sağa
            drawPacmanRight(g2d);
        } else if (view_dy == -1) { // y miz -1 ise yukarı
            drawPacmanUp(g2d);
        } else { //y 1 ise aşağı çizdiriyor
            drawPacmanDown(g2d);
        }
    }

    private void drawPacmanUp(Graphics2D g2d) {

        switch (pacmanAnimPos) {
            case 1:
                g2d.drawImage(pacman2up, pacman_x + 1, pacman_y + 1, this); // ağzı az açık pacmanimiz x ve y konumunca yukarı gider
                break;
            case 2:
                g2d.drawImage(pacman3up, pacman_x + 1, pacman_y + 1, this); // ağzı yarı açık
                break;
            case 3:
                g2d.drawImage(pacman4up, pacman_x + 1, pacman_y + 1, this); // ağzı tam açık
                break;
            default:
                g2d.drawImage(pacman1, pacman_x + 1, pacman_y + 1, this);   // normal pacman fotoğrafımız
                break;

        }
    }

    private void drawPacmanDown(Graphics2D g2d) {  //pacmani aşağı çizdirme metodu

        switch (pacmanAnimPos) {
            case 1:
                g2d.drawImage(pacman2down, pacman_x + 1, pacman_y + 1, this); //ağzı az açık pacmanimiz x ve y konumunca aşağı gider
                break;
            case 2:
                g2d.drawImage(pacman3down, pacman_x + 1, pacman_y + 1, this);// ağzı yarı açık
                break;
            case 3:
                g2d.drawImage(pacman4down, pacman_x + 1, pacman_y + 1, this);// ağzı tam açık
                break;
            default:
                g2d.drawImage(pacman1, pacman_x + 1, pacman_y + 1, this); // normal pacman fotoğrafımız
                break;
        }
    }

    private void drawPacnanLeft(Graphics2D g2d) {  // pacmani sola çizdir

        switch (pacmanAnimPos) {
            case 1:
                g2d.drawImage(pacman2left, pacman_x + 1, pacman_y + 1, this);//ağzı az açık pacmanimiz x ve y konumunca sola gider
                break;
            case 2:
                g2d.drawImage(pacman3left, pacman_x + 1, pacman_y + 1, this);// ağzı yarı açık
                break;
            case 3:
                g2d.drawImage(pacman4left, pacman_x + 1, pacman_y + 1, this);// ağzı tam açık
                break;
            default:
                g2d.drawImage(pacman1, pacman_x + 1, pacman_y + 1, this);// normal pacman fotoğrafımız
                break;
        }
    }

    private void drawPacmanRight(Graphics2D g2d) {// pacmani sağa çizdir

        switch (pacmanAnimPos) {
            case 1:
                g2d.drawImage(pacman2right, pacman_x + 1, pacman_y + 1, this);//ağzı az açık pacmanimiz x ve y konumunca sağa gider
                break;
            case 2:
                g2d.drawImage(pacman3right, pacman_x + 1, pacman_y + 1, this);// ağzı yarı açık
                break;
            case 3:
                g2d.drawImage(pacman4right, pacman_x + 1, pacman_y + 1, this);// ağzı tam açık
                break;
            default:
                g2d.drawImage(pacman1, pacman_x + 1, pacman_y + 1, this);// normal pacman fotoğrafımız
                break;
        }
    }

    private void drawMaze(Graphics2D g2d) {  //labirent çiz

        short i = 0;
        int x, y;

        for (y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE) {
            for (x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE) {

                g2d.setColor(mazeColor);
                g2d.setStroke(new BasicStroke(2));

                if ((screenData[i] & 1) != 0) {
                    g2d.drawLine(x, y, x, y + BLOCK_SIZE - 1); // labirentin sol çizgisini çizdirir
                }

                if ((screenData[i] & 2) != 0) {
                    g2d.drawLine(x, y, x + BLOCK_SIZE - 1, y);// labirentin yukarı çizgisini çizdirir
                }

                if ((screenData[i] & 4) != 0) {
                    g2d.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1,// labirentin sağ çizgisini çizdirir
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 8) != 0) {
                    g2d.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1,// labirentin aşağı çizgisini çizdirir
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 16) != 0) {  // noktalarımızı çizdiriyoruz
                    g2d.setColor(dotColor);
                    g2d.fillRect(x + 11, y + 11, 2, 2);
                }

                i++;
            }
        }
    }

    private void initGame() {   // oyuna başlıyoruz

        pacsLeft = 3;  //3 tane pacman canı var
        score = 0;
        initLevel();
        N_GHOSTS = 6; // hayalet sayımız
        currentSpeed = 3;
    }

    private void initLevel() {

        int i;
        for (i = 0; i < N_BLOCKS * N_BLOCKS; i++) {
            screenData[i] = levelData[i];   // bloktaki tüm yemleri yedikten sonra level atlarız  atlayamazsak bölümü tekrarlarız
        }

        continueLevel();
    }

    private void continueLevel() {

        short i;
        int dx = 1;
        int random;

        for (i = 0; i < N_GHOSTS; i++) {  // yeni levelde hayalet sayımızı arttırdık

            ghost_y[i] = 4 * BLOCK_SIZE;
            ghost_x[i] = 4 * BLOCK_SIZE;
            ghost_dy[i] = 0;
            ghost_dx[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (currentSpeed + 1));

            if (random > currentSpeed) {
                random = currentSpeed;
            }

            ghostSpeed[i] = validSpeeds[random]; // hayaletlere rastgele random bir şekilde hız veriyor
        }

        pacman_x = 7 * BLOCK_SIZE;
        pacman_y = 11 * BLOCK_SIZE;
        pacmand_x = 0;
        pacmand_y = 0;
        req_dx = 0;
        req_dy = 0;
        view_dx = -1;
        view_dy = 0;
        dying = false;
    }

    private void loadImages() {

        ghost = new ImageIcon("src//resources//ghost.png").getImage();
        pacman1 = new ImageIcon("src//resources//pacman.png").getImage();
        pacman2up = new ImageIcon("src//resources//up1.png").getImage();
        pacman3up = new ImageIcon("src//resources//up2.png").getImage();
        pacman4up = new ImageIcon("C:src//resources//up3.png").getImage();
        pacman2down = new ImageIcon("src//resources//down1.png").getImage();
        pacman3down = new ImageIcon("src//resources//down2.png").getImage();
        pacman4down = new ImageIcon("src//resources//down3.png").getImage();
        pacman2left = new ImageIcon("src//resources//left1.png").getImage();
        pacman3left = new ImageIcon("src//resources//left2.png").getImage();
        pacman4left = new ImageIcon("src//resources//left3.png").getImage();
        pacman2right = new ImageIcon("src//resources//right1.png").getImage();
        pacman3right = new ImageIcon("src//resources//right2.png").getImage();
        pacman4right = new ImageIcon("src//resources//right3.png").getImage();

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g); // her şeyimizi bu metodla çizdirdik

        doDrawing(g);
    }

    private void doDrawing(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.black); // oyunun arka planı
        g2d.fillRect(0, 0, d.width, d.height); // kare çizdirmek

        drawMaze(g2d); // labirent çizdir
        drawScore(g2d);
        doAnim(); // bunu yazmazsak ağız açıp kapama yapmıyor

        if (inGame) {
            playGame(g2d); //oyunun içi
        } else {
            showIntroScreen(g2d);//  eğer oyunun içinde değilsen giriş ekranı geliyor
        }

        g2d.drawImage(ii, 5, 5, this);
        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }

    class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();  // pacmani hareket ettirmeye yarıyor

            if (inGame) {
                if (key == KeyEvent.VK_LEFT) {  // sola hareket
                    req_dx = -1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_RIGHT) { // sağa hareket
                    req_dx = 1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_UP) { // yukarı hareket
                    req_dx = 0;
                    req_dy = -1;
                } else if (key == KeyEvent.VK_DOWN) { // aşağı hareket
                    req_dx = 0;
                    req_dy = 1;
                } else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    inGame = false;    // esc oyundan çıkıyorsun
                } else if (key == KeyEvent.VK_PAUSE) { // pause oyunu durdurur
                    if (timer.isRunning()) {
                        timer.stop();
                    } else {
                        timer.start();
                    }
                }
            } else {
                if (key == 's' || key == 'S') {
                    inGame = true;
                    initGame();  // s ve büyük s ye bssarsan oyun başlar
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {

            int key = e.getKeyCode();

            if (key == Event.LEFT || key == Event.RIGHT
                    || key == Event.UP || key == Event.DOWN) {
                req_dx = 0;
                req_dy = 0;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {  //klavyeye aktiflik veriyor

        repaint();
    }
}