package gunslinger.dumb;

import java.util.*;

// An example player
// Extends gunslinger.sim.Player to start with your player
//
public class Player extends gunslinger.sim.Player
{
    // total versions of the same player
    private static int versions = 0;
    // my version no
    private int version = versions++;
    
    // A simple fixed shoot rate strategy used by the dumb player
    //    private static double ShootRate = 0.8;

    // name of the team
    //
    public String name()
    {
        return "10";
    }
 
    // Initialize the player
    //
    public void init(int nplayers, int[] friends, int enemies[])
    {
        // Note:
        //  Seed your random generator carefully
        //  if you want to repeat the same random number sequence
        //  pick your favourate seed number as the seed
        //  Or you can simply use the clock time as your seed     
        //       
        gen = new Random(System.currentTimeMillis());
        // long seed = 12345;
        // gen = new Random(seed);

        this.nplayers = nplayers;

        for (int i = 0; i != friends.length; i++)
            this.friends.add(friends[i]);

        for (int i = 0; i != enemies.length; i++)
            this.enemies.add(enemies[i]);
    }

    // Pick a target to shoot
    // Parameters:
    //  prevRound - an array of previous shoots, prevRound[i] is the player that player i shot
    //              -1 if player i did not shoot
    //              In the first round, prevRound = null
    //  alive - an array of player's status, true if the player is still alive in this round
    // Return:
    //  int - the player id to shoot, return -1 if do not shoot anyone
    //
    public int shoot(int[] prevRound, boolean[] alive)
    {
        /* Strategy used by the dumb player:
           Decide whether to shoot or not with a fixed shoot rate
           If decided to shoot, randomly pick one alive that is not your friend */
                
        // Shoot or not in this round?
        //        boolean shoot = gen.nextDouble() < ShootRate;

        // if (!shoot)
        //     return -1;

        ArrayList<Integer> targets = new ArrayList<Integer>();
        for (int i = 0; i != nplayers; ++i)
            if (i != id && alive[i] && !friends.contains(i))
                targets.add(i);

        if (targets.size() == 0)
            return -1;
        
        int target = targets.get(gen.nextInt(targets.size()));

        return target;
    }


    private Random gen;
    private int nplayers;
    private ArrayList<Integer> friends = new ArrayList<Integer>();
    private ArrayList<Integer> enemies = new ArrayList<Integer>();
}
