

import java.awt.EventQueue;
import javax.swing.JFrame;

public class Pacman extends JFrame {

    public Pacman() {
        
        initialize();
    }
    
    private void initialize() {
        
        add(new Board());
        
        setTitle("PacMan");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450,450);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    
    

    public static void main(String[] args) {

        
            var ex = new Pacman();
           
       
    }
}