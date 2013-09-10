package gunslinger.Group3Player;

import java.util.*;

// The base class of a player
// Extends the base class to start your player
// See dumb/Player.java for an example
//
public class Player extends gunslinger.sim.Player
{

    private int nplayers;
    private int[] friends;
    private int[] enemies;
    
    // total versions of the same player
    private static int versions = 0;
    // my version no
    private int version = versions++;
    
    // name of the team
    //
    public String name()
    {
        return "Group3Player" + (versions > 1 ? " v" + version : "");
    }
    
    // Initialize the player
    //
    public void init(int nplayers, int[] friends, int enemies[])
    {
    	this.nplayers = nplayers;
    	this.friends = friends;
    	this.enemies = enemies;    	
    }

    // Pick a target to shoot
    // Parameters:
    //  prevRound - an array of previous shoots, prevRound[i] is the player that player i shot
    //              -1 if player i did not shoot
    //  alive - an array of player's status, true if the player is still alive in this round
    // Return:
    //  int - the player id to shoot, return -1 if do not shoot anyone
    //
    public int shoot(int[] prevRound, boolean[] alive){
    	
    	for(int j = 0;j < prevRound.length; j++)
    	{
    		if(Arrays.asList(enemies).contains(prevRound[j]) && alive[prevRound[j]] && prevRound[j]!=id)
    		{
    			return prevRound[j];
    		}	
    	}
    	
    	return -1;
    	
    }
    
}
