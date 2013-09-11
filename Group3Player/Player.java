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
        this.friends = friends.clone();
        this.enemies = enemies.clone();  	
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
    	
    	
    	
    	if (prevRound == null)
    	{
    		//First Round Strategy -> wait do nothing
    		System.err.println("[ME] First Round, I am " + id + " waiting...");
    	}
    	else
    	{
    		System.err.println("[ME] I am " + id);
    		
	    	//Shoot person you shot at before if not dead

    		int lastPersonShotAt = prevRound[id];
	    		
	    	if( lastPersonShotAt != -1 && alive[lastPersonShotAt] )
	    	{
				System.err.println("[ACTION] Trying to kill player  " + lastPersonShotAt + " since last time they did not die when I shot them");
	    		return lastPersonShotAt;
	    	}

    		
	    	//Prioritize person who shot you
	    	for(int i = 0;i < prevRound.length; i++)
	    	{
	    		if( (prevRound[i] == id) && alive[i] )
	    		{
				
					System.err.println("[ACTION] Player " + i + " shot me, going to shoot back");
	    			return i;
	    		}
	       	}
			
			//Shoot at Enemies that shot at friends
			for(int i = 0;i < prevRound.length; i++)
			{
				if( Arrays.asList(friends).contains(prevRound[i]) && alive[i] && Arrays.asList(enemies).contains(i) )
				{
					System.err.println("[ACTION] Shooting at " + i + " since they shot my friend and they are my enemy");
					return i;
				}
			}
    	}
    	
    	

//    	//Shoot to kill
//    	for(int j = 0;j < prevRound.length; j++)
//    	{
//    		if(Arrays.asList(enemies).contains(prevRound[j]) && alive[j] && prevRound[j]!=id)
//    		{
//    			return prevRound[j];
//    		}	
//    	}
    	
    	return -1;
    	
    }
    
}
