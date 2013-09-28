package gunslinger.dumb2;

import java.util.*;

// An example player
// Extends gunslinger.sim.Player to start with your player
//
public class Player extends gunslinger.sim.Player
{    
    // name of the team
    //
    public String name()
    {
        return "11";
    }
 
    // Initialize the player
    //
    public void init(int nplayers, int[] friends, int enemies[])
    {

        this.nplayers = nplayers;

        for (int i = 0; i != friends.length; i++)
            this.friends.add(friends[i]);

        for (int i = 0; i != enemies.length; i++)
            this.enemies.add(enemies[i]);
    }

    public int shoot(int[] prevRound, boolean[] alive)
    {
                
        for (int i = 0; i != enemies.size(); ++i) {
            if (i != id && alive[enemies.get(i)])
                return enemies.get(i);
        }

        return -1;
    }


    private int nplayers;
    private ArrayList<Integer> friends = new ArrayList<Integer>();
    private ArrayList<Integer> enemies = new ArrayList<Integer>();
}
