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

    private Dimension d;
    private final Font smallFont = new Font("Helvetica", Font.BOLD, 14);

    private Image ii;
    private final Color dotColor = new Color(192, 192, 0);
    private Color mazeColor;
    
    private boolean st=true;
    private boolean inGame = false;
    private boolean dying = false;

    private final int BLOCK_SIZE = 24;
    private final int N_BLOCKS = 15;
    private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE;
    private final int PAC_ANIM_DELAY = 2;
    private final int PACMAN_ANIM_COUNT = 5;
    private final int MAX_GHOSTS = 6;
    private final int PACMAN_SPEED = 4;

    private int pacAnimCount = PAC_ANIM_DELAY;
    private int pacAnimDir = 1;
    private int pacmanAnimPos = 0;
    private int N_GHOSTS = 6;
    private int pacsLeft, score;
    private int[] dx, dy;
    private int[] ghost_x, ghost_y, ghost_dx, ghost_dy, ghostSpeed;

    private Image ghost;
    private Image pacmanup, pacmanleft, pacmanright, pacmandown;

    private int pacman_x, pacman_y, pacmand_x, pacmand_y;
    private int req_dx, req_dy, view_dx, view_dy;
    
    //data depicting the various cell containing certain values
    private final short levelData[] = {
        19, 26, 26, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
        21, 0, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        21, 0, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        21, 0, 0, 17, 16, 16, 16, 24, 16, 16, 16, 16, 16, 16,20,
        17, 18, 18, 16, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 20,
        17, 16, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 16, 24, 20,
        25, 16, 16, 16, 24, 24, 28, 0, 25, 24, 24, 16, 20, 0, 21,
        1, 17, 16, 20, 0, 0, 0, 0, 0, 0, 0, 17, 20, 0, 21,
        1, 17, 16, 16, 18, 18, 22, 0, 19, 18, 18, 16, 20, 0, 21,
        1, 17, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 20, 0,21,
        1, 17, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 20, 0, 21,
        1, 17, 16, 16, 16, 16, 16, 18, 16, 16, 16, 16, 20, 0, 21,
        1, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,21,
        1, 25, 24, 24, 24, 24, 24, 24, 24, 24, 16, 16, 16, 18, 20,
        9, 8, 8, 8, 8, 8, 8, 8, 8, 8, 25, 24, 24, 24,28
    };
//number 1,2,4,8 represent the left,top,right and bottom corners.and 16 is a point
   //numbers can be added as 19 (16+1+2) will have a point with left and top corner
    private final int validSpeeds[] = {1, 2, 3, 4, 6, 8};//speeds for the ghosts
    private final int maxSpeed = 6;

    private int currentSpeed = 3;
    private short[] screenData;
    private Timer timer;

    public Board() {
        loadImages();
        initVariables(); 
        initBoard();
    }
    
    private void initBoard() {
        
        addKeyListener(new TAdapter());// receive key events from this component

        setFocusable(true);//focused state

        setBackground(Color.black);//background color is set as black
    }

    private void initVariables() {

        screenData = new short[N_BLOCKS * N_BLOCKS];
        mazeColor = new Color(5, 100, 5);
        d = new Dimension(500, 500); //dimensions of the gaming area
        ghost_x = new int[MAX_GHOSTS];//x co-ordinate of the ghost 
        ghost_dx = new int[MAX_GHOSTS];
        ghost_y = new int[MAX_GHOSTS];
        ghost_dy = new int[MAX_GHOSTS];
        ghostSpeed = new int[MAX_GHOSTS];
        dx = new int[6];
        dy = new int[6];
        
        timer = new Timer(50, this);//Create a Timer and initializes both the initial delay and between-event delay to delay milliseconds. 
        timer.start();//start the timer
    }

    @Override
    public void addNotify() {
        super.addNotify();//the chain of parent components is set up with KeyboardAction event listeners. 

        startGame();
    }

    

    private void playGame(Graphics2D g2d) {

        if (dying) {

            death();//

        } else {

            movePacman();
            drawPacman(g2d);
            moveGhosts(g2d);
            checkMaze();
        }
    }

    private void startScreen(Graphics2D g2d) {

        g2d.setColor(Color.blue);//color of the string displaying rectangle
        g2d.fillRect(50, SCREEN_SIZE / 2 - 30, SCREEN_SIZE - 100, 50);
      
        String s = "Press Enter to start.";
        Font small = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics metr = this.getFontMetrics(small);

        g2d.setColor(Color.white);
        g2d.setFont(small);
        g2d.drawString(s, (SCREEN_SIZE - metr.stringWidth(s)) / 2, SCREEN_SIZE / 2);
    }
    
    
    private void startScreenAgain(Graphics2D g2d) {

        g2d.setColor(Color.blue);//color of the string displaying rectangle
        g2d.fillRect(50, SCREEN_SIZE / 2 - 30, SCREEN_SIZE - 100, 50);
      

        String s = "You lose.Press Enter to start again";
        Font small = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics metr = this.getFontMetrics(small);
        score=0;
        g2d.setColor(Color.white);
        g2d.setFont(small);
        g2d.drawString(s, (SCREEN_SIZE - metr.stringWidth(s)) / 2, SCREEN_SIZE / 2);
    }

    private void drawScore(Graphics2D g) {

        int i;
        String s;

        g.setFont(smallFont);
        g.setColor(new Color(96, 128, 255));
        s = "Score: " + score;
        g.drawString(s, SCREEN_SIZE / 2 + 96, SCREEN_SIZE + 16);

        for (i = 0; i < pacsLeft; i++) {
            g.drawImage(pacmanleft, i * 28 + 8, SCREEN_SIZE + 1, this);//drawing the lives representing pac man images
        }
    }

    private void checkMaze() {

        short i = 0;
        boolean finished = true;

        while (i < N_BLOCKS * N_BLOCKS && finished) {

            if ((screenData[i] & 48) != 0) {
                finished = false;
            }

            i++;
        }

        if (finished) {

            score += 50;

            initLevel();
        }
    }

    private void death() {

        pacsLeft--;// reducing the lifes of the player on dying

        if (pacsLeft == 0) {
            inGame = false;//stop the game on losing all the games
            st=false;
        }

        continueLevel();//continue the game
    }

    private void moveGhosts(Graphics2D g2d) {

        short i;
        int pos;
        int count;

        for (i = 0; i < N_GHOSTS; i++) {
            if (ghost_x[i] % BLOCK_SIZE == 0 && ghost_y[i] % BLOCK_SIZE == 0) {
                pos = ghost_x[i] / BLOCK_SIZE + N_BLOCKS * (int) (ghost_y[i] / BLOCK_SIZE);

                count = 0;
                //movement of the ghosts left, right,top and bottom side
                if ((screenData[pos] & 1) == 0 && ghost_dx[i] != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 2) == 0 && ghost_dy[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }

                if ((screenData[pos] & 4) == 0 && ghost_dx[i] != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 8) == 0 && ghost_dy[i] != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                if (count == 0) {

                    if ((screenData[pos] & 15) == 15) {
                        ghost_dx[i] = 0;
                        ghost_dy[i] = 0;
                    } else {
                        ghost_dx[i] = -ghost_dx[i];
                        ghost_dy[i] = -ghost_dy[i];
                    }

                }else{

                   count = (int) (Math.random() * count);

                    if (count > 3) {
                        count = 3;
                    }

                    ghost_dx[i] = dx[count];
                    ghost_dy[i] = dy[count];
                }
            }
                    
            
            
            ghost_x[i] = ghost_x[i] + (ghost_dx[i] * ghostSpeed[i]);
            ghost_y[i] = ghost_y[i] + (ghost_dy[i] * ghostSpeed[i]);
            drawGhost(g2d, ghost_x[i] + 1, ghost_y[i] + 1);
            //collision of the ghost with the pacman leads to life loss
            if (pacman_x > (ghost_x[i] - 12) && pacman_x < (ghost_x[i] + 12)
                    && pacman_y > (ghost_y[i] - 12) && pacman_y < (ghost_y[i] + 12)
                    && inGame) {

                dying = true;
            }
        }
       
        
    }

    private void drawGhost(Graphics2D g2d, int x, int y) {

        g2d.drawImage(ghost, x, y, this);
    }

    private void movePacman() {

        int pos;
        short ch;
        //exhibit pac man from moving off the screen
        if (req_dx == -pacmand_x && req_dy == -pacmand_y) {
            pacmand_x = req_dx;
            pacmand_y = req_dy;
            view_dx = pacmand_x;
            view_dy = pacmand_y;
        }
		
        if (pacman_x % BLOCK_SIZE == 0 && pacman_y % BLOCK_SIZE == 0) {
            pos = pacman_x / BLOCK_SIZE + N_BLOCKS * (int) (pacman_y / BLOCK_SIZE);
            ch = screenData[pos];//get the position of the pac man
            //if pacman eats circle score increases
            if ((ch & 16) != 0) {
                screenData[pos] = (short) (ch & 15);
                score++;
            }
            //check for the movement of pac man
            if (req_dx != 0 || req_dy != 0) {
                if (!((req_dx == -1 && req_dy == 0 && (ch & 1) != 0)
                        || (req_dx == 1 && req_dy == 0 && (ch & 4) != 0)
                        || (req_dx == 0 && req_dy == -1 && (ch & 2) != 0)
                        || (req_dx == 0 && req_dy == 1 && (ch & 8) != 0))) {
                    pacmand_x = req_dx;
                    pacmand_y = req_dy;
                    view_dx = pacmand_x;
                    view_dy = pacmand_y;
                }
            }

            // Check for standstill
            if ((pacmand_x == -1 && pacmand_y == 0 && (ch & 1) != 0)
                    || (pacmand_x == 1 && pacmand_y == 0 && (ch & 4) != 0)
                    || (pacmand_x == 0 && pacmand_y == -1 && (ch & 2) != 0)
                    || (pacmand_x == 0 && pacmand_y == 1 && (ch & 8) != 0)) {
                pacmand_x = 0;
                pacmand_y = 0;
            }
        }
        //increment in the position of the pacman
        pacman_x = pacman_x + PACMAN_SPEED * pacmand_x;
        pacman_y = pacman_y + PACMAN_SPEED * pacmand_y;
    }

    private void drawPacman(Graphics2D g2d) {
    	//drawing different images depicting the left, right, up and down position of the pacman
        if (view_dx == -1) {
            g2d.drawImage(pacmanleft, pacman_x + 1, pacman_y + 1, this);
        } else if (view_dx == 1) {
        	 g2d.drawImage(pacmanright, pacman_x + 1, pacman_y + 1, this);
        } else if (view_dy == -1) {
        	g2d.drawImage(pacmanup, pacman_x + 1, pacman_y + 1, this);
        } else {
        	g2d.drawImage(pacmandown, pacman_x + 1, pacman_y + 1, this);
        }
    }

   

    private void drawMaze(Graphics2D g2d) {

        short i = 0;
        int x, y;
        
        
        for (y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE) {
            for (x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE) {

                
               g2d.setColor(mazeColor);
        g2d.setStroke(new BasicStroke(2));
                if ((screenData[i] & 1) != 0) { 
                    g2d.drawLine(x, y, x, y + BLOCK_SIZE - 1);//drawing vertical left line
                }

                if ((screenData[i] & 2) != 0) { 
                    g2d.drawLine(x, y, x + BLOCK_SIZE - 1, y);//drawing horizontal left line
                }
				
                if ((screenData[i] & 4) != 0) { 
                    g2d.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1,//drawing vertical right line
                            y + BLOCK_SIZE - 1);
                }
				
                if ((screenData[i] & 8) != 0) { 
                    g2d.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1,//drawing the horizontal right line
                            y + BLOCK_SIZE - 1);
                }
			
                if ((screenData[i] & 16) != 0) { //marking the dots where there is no wall or wall occupied area
                    g2d.setColor(dotColor);
                    g2d.fillRect(x + 11, y + 11, 2, 2);
                }
                
                i++;
                }
            }
        
    }

    private void startGame() {

        pacsLeft = 5;//lives left
        score = 0;
        initLevel();
        N_GHOSTS = 6;
        currentSpeed = 3;
    }

    private void initLevel() {

        int i;
        for (i = 0; i < N_BLOCKS * N_BLOCKS; i++) {
            screenData[i] = levelData[i];
        }

        continueLevel();//continue the game
    }

    private void continueLevel() {

        short i;
        int random;
        int dx=1;
        for (i = 0; i < N_GHOSTS; i++) {

            ghost_y[i] = 4 * BLOCK_SIZE;//specifying the start co-ordinates of the ghost
            ghost_x[i] = 4 * BLOCK_SIZE;
            ghost_dy[i] = 0;//
           //ghost_dx[i] = dx;
            dx = - dx;
            //specifying the speed of every ghost randomly
            random = (int) (Math.random() * (currentSpeed + 1));//generating the speed of the ghost

           if (random > currentSpeed) {
                random = currentSpeed;
            }

            ghostSpeed[i] = validSpeeds[random];//specifying the speed of each ghost
            
        }

        pacman_x = 7 * BLOCK_SIZE;//starting x co-ordinate of pacman
        pacman_y = 11 * BLOCK_SIZE;//starting y cordinate of pacman
       ////pacman remains at rest until a key is trigerred to move it 
        pacmand_x = 0;
        pacmand_y = 0;
        req_dx = 0;
        req_dy = 0;
        //view_dx = -1;
        //view_dy = 0;
        dying = false;//continue the game till all lives are not removed
    }

    public void loadImages() {
    	//load the images of the game i.e. ghost and pac man from the source folder
    	//different images being used for the up, down, top and down movement of the pac man
        ghost = new ImageIcon("images/ghost.png").getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT);
      
        pacmanup = new ImageIcon("images/up.png").getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT);
        
        pacmandown = new ImageIcon("images/down.png").getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT);
        
        pacmanleft = new ImageIcon("images/left.png").getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT);
        
        pacmanright = new ImageIcon("images/right.png").getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT);
        
         
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);// initiate the drawing function for the 
    }

    private void doDrawing(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;// class for rendering2-dimensional shapes, text and images on the Java platform. 

        g2d.setColor(Color.black);//set background color of the game as black
       
        drawMaze(g2d);//draw the maze
        drawScore(g2d);//draw the score board
       
        if (inGame) {
            playGame(g2d);
        } else if(st){
            startScreen(g2d);//show the start screen
        }else
        {
        	startScreenAgain(g2d);
        }

    }

    class TAdapter extends KeyAdapter {

        @Override
        //movement of the pacman
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if (inGame) {
                if (key == KeyEvent.VK_LEFT) {
                    req_dx = -1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_RIGHT) {
                    req_dx = 1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_UP) {
                    req_dx = 0;
                    req_dy = -1;
                } else if (key == KeyEvent.VK_DOWN) {
                    req_dx = 0;
                    req_dy = 1;
                } else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    inGame = false;
                } else if (key == KeyEvent.VK_P) {//pause the game
                    if (timer.isRunning()) {
                        timer.stop();
                    } 
                }else {
                	if(key==KeyEvent.VK_R)//resume the game
                        timer.start();
                    }
                
            } else {
                if (key ==KeyEvent.VK_ENTER) {//start the game
                    inGame = true;
                    startGame();
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {

          
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        repaint();
    }
}
