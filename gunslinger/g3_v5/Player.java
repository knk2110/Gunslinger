package gunslinger.g3;
import java.util.*;
import java.io.*;

public class Player extends gunslinger.sim.Player
{
    private int nplayers, nfriends, nenemies, nneutrals;
    private int[] friends, enemies, neutrals, allegiance;
	private int roundNum = -1, oldRounds = 2, prevRoundNum;
    private int[][] history, shooter;
	private double[][] friendship;
	private boolean[][] relationship;

	boolean createLog;
	private PrintWriter outfile;

    private static int versions = 0;
    private int version = versions++;
    public String name() { return "g3" + (versions > 1 ? " v" + version : ""); }

    public void init(int nplayers, int[] friends, int enemies[])
    {
		createLog = false;

		this.nplayers = nplayers;
        this.friends = friends.clone();
        this.enemies = enemies.clone();
		nfriends = friends.length;
		nenemies = enemies.length;
		nneutrals = nplayers - nfriends - nenemies;

		history = new int[100][nplayers];

		for (int i = 0; i < history.length; i++)
			for (int j = 0; j < nplayers; j++)
				history[i][j] = -1;

		relationship = new boolean[nplayers][nplayers];
		friendship = new double[nplayers][nplayers];

		for (int i = 0; i < nplayers; i++)
			for (int j = 0; j < nplayers; j++)
				relationship[i][j] = true;

		shooter = new int[oldRounds][nplayers];

		for(int r = 0; r < oldRounds; r++)
			for(int i = 0; i < nplayers; i++)
					shooter[r][i] = -1;

		allegiance = new int[nplayers];
		for(int i = 0; i < nfriends; i++)
		{
			allegiance[friends[i]] = 1;
			friendship[id][friends[i]] = +50.0;
			friendship[friends[i]][id] = +50.0;
		}
		for(int i = 0; i < nenemies; i++)
			allegiance[enemies[i]] = -1;
		allegiance[id] = 2;

		if(createLog) try {
			FileWriter fstream = new FileWriter("gunslinger/g3/log.txt", true);
			outfile = new PrintWriter(fstream);
			//outfile.println("Init:");
			//outfile.println(Integer.toString(id));
			//outfile.println("\n" + Arrays.toString(allegiance));
			//outfile.flush();
		} catch (Exception e){ }
	}

	private void updateHistory(int[] prevRound, boolean[] alive)
	{
		for (int i = 0; i < nplayers; i++) // Updating history
			history[prevRoundNum][i] = prevRound[i];
		if(roundNum == 100) // Shifting history if rounds exceed 100
		{
			for(int r = 50; r < 100; r++)
				for (int i = 0; i < nplayers; i++)
					history[r - 50][i] = history[r][i];
			roundNum = roundNum - 50;
		}

		for(int r = oldRounds - 1; r > 0; r--) // Updating shooter
			for(int i = 0; i < nplayers; i++)
					shooter[r][i] = shooter[r-1][i];
			for(int i = 0; i < nplayers; i++)
				shooter[0][i] = -1;

		for(int i = 0; i < nplayers; i++)
			// If a player is shot, and both he and his attacker is alive, he has a shooter
			if(alive[i] && history[prevRoundNum][i] != -1 && alive[history[prevRoundNum][i]])
				shooter[0][history[prevRoundNum][i]] = i;
	}

	private final double SUPPORT_FRIEND  = +1.0;
	private final double DEFEND_FRIEND   = +3.0;

	private void updateRelations(boolean[] alive)
	{
		int target, victim;
		for (int i = 0; i < nplayers; i++)
		{
			target = history[prevRoundNum][i];
			if(target != -1)
			{
				//relationship set to false if one player shoots another. default: true
				relationship[i][target] = false;
				relationship[target][i] = false;
				if (roundNum > 1)
					// Removing consistency and retaliation
					if(shooter[1][i] != target && shooter[1][target] != i)
					{ //Needs to be modified for when shooter is also victim
						if(shooter[1][target] != -1) // If the target had an attacker
						{
							friendship[i][shooter[1][target]] += SUPPORT_FRIEND;
							friendship[shooter[1][target]][i] += SUPPORT_FRIEND;
						}
						victim = history[prevRoundNum - 1][target];
						if(victim != -1) // If the target shot someone last round
						{
							friendship[i][victim] += DEFEND_FRIEND;
							friendship[victim][i] += DEFEND_FRIEND;
						}
					}
			}
		}
	}

	private final double ENGAGE_DUEL      = +1.0;
	private final double ENGAGE_TARGET    = +0.5;
	private final double ENGAGE_SHOOTER   = +0.5;
	private final double ATTACK_TARGET    = +0.8;
	private final double DEFEND_SELF      = +0.9;
	private double EXPECTED_ACTIVITY      = +0.0;

	//done b/c everyone became too conservative
	private double[] getExpectedShots(boolean[] alive)
	{
		double[] expectedShots = new double[nplayers];
		int target;

		//active players are players that shot the previous round OR if someone shot them
		//if A->B and C->B and B dies, A, B, and C are inactive
		//theory: if a player is going to shoot, they will focus on the active players (this assumption is made based on the conservative AIs we saw toward the later part of this assignment)
		double playersAlive = 0, playersActive = 0, shotsFired = 0, playersInactive;
		for(int i = 0; i < nplayers; i++) // Finding the intensity of shooting activity
			if(alive[i])
			{
				playersAlive++;
				target = history[prevRoundNum][i];
				if(target != -1)
					shotsFired++;
				if(shooter[0][i] != -1 || (target != -1 && alive[target]))
					playersActive++;
			}
		playersInactive = playersAlive - playersActive;
		if((history[prevRoundNum][id] == -1 || !alive[history[prevRoundNum][id]]) && shooter[0][id] == -1) // Excluding self from inactive list
			playersInactive -= 1;

		//these are approximates of the number of friends and enemies alive (again, excluding our player)
		double enemiesAlive = nenemies*(playersAlive-1)/(nplayers-1);	
		double friendsAlive = nfriends*(playersAlive-1)/(nplayers-1);
		double enemyProb = enemiesAlive/(playersAlive-friendsAlive-1);	//probability that someone has an enemy in the set of active players(subtract 1 for the player itself)
		EXPECTED_ACTIVITY = 0.0;
		/** assume that each inactive player will shoot an active player (possibly bad assumption, but we're using it for simplicity)
		support friend factor is not present, need to add a + factor for how many players have friends that are active (see below)
		*/
		//if we shoot B, expected number of shots for B for next round is 0 because we are only concerned with what is going on with everyone else; we know what we are going to do.
		if(playersActive > 0)
			EXPECTED_ACTIVITY = playersInactive * enemyProb / playersActive;

		for(int i = 0; i < nplayers; i++) // Who would each player shoot?
			if(i != id && alive[i])
			{
				target = history[prevRoundNum][i];
				if(target != -1 && alive[target] && shooter[0][i] != -1) // If the player shot someone, and also got shot by someone
				{
					if(target == shooter[0][i]) // If the target and shooter are the same
						expectedShots[target] += ENGAGE_DUEL; //A->B and B->A
					else
					{
						expectedShots[target] += ENGAGE_TARGET; 
						expectedShots[shooter[0][i]] += ENGAGE_SHOOTER;
					}
				}
				else if(target != -1 && alive[target]) // If the player shot someone
					expectedShots[target] += ATTACK_TARGET;
				else if(shooter[0][i] != -1) // If the player was shot
					expectedShots[shooter[0][i]] += DEFEND_SELF;
			}

		for(int i = 0; i < nplayers; i++) // How would inactive players respond to active incidents
		{
			target = history[prevRoundNum][i];
			if(target != -1 && alive[target] && shooter[0][i] != -1)
				expectedShots[i] += EXPECTED_ACTIVITY * 1.2;
			else if((target != -1 && alive[target]) || shooter[0][i] != -1)
				expectedShots[i] += EXPECTED_ACTIVITY * 0.8;
		}

		return expectedShots;
	}

	//assume number of active players this round is the same as the number of active players in the previous round, for simplicity
	//this method estimates the number of people that have us as an enemy. it allows us to determine what our chance of dying in two rounds is if we shoot a player in the next round.
	private double getExpectedRetaliation(boolean[] alive)
	{
		double expectedRetaliation = 0;
		int target;
		double playersAlive = 0, playersActive = 0, myFriendsAlive = 0;
		for(int i = 0; i < nplayers; i++) // Finding the intensity of shooting activity
			if(alive[i])
			{
				playersAlive++;
				target = history[prevRoundNum][i];
				if(shooter[0][i] != -1 || (target != -1 && alive[target]))
					playersActive++;
				else if(i == id) // Include self to active list
					playersActive++;
				if(allegiance[i] == 1)
					myFriendsAlive++;
			}
		double enemiesAlive = nenemies*(playersAlive-myFriendsAlive-2)/(nplayers-nfriends-1);
		double friendsAlive = (nfriends*(playersAlive-1)/(nplayers-1))-myFriendsAlive;

		if(enemiesAlive > 0)
			expectedRetaliation += enemiesAlive / playersActive;
		if(friendsAlive > 0)
			expectedRetaliation += friendsAlive / playersActive;

		return expectedRetaliation;
	}
	//possible future expansion: calculate the above values for each player as opposed to a general player

	private final double ACTION_THRESHOLD_1    = +0.8;
	private final double ACTION_THRESHOLD_2    = +1.1;
	private final double ELIMINATION_THRESHOLD = +1.6;

    public int shoot(int[] prevRound, boolean[] alive)
    {

		roundNum++;
		prevRoundNum = roundNum - 1;

		if(createLog) try {
			//outfile.println("\nRound " + Integer.toString(roundNum) + ":");
			//outfile.println(Arrays.toString(prevRound));
			outfile.println(Arrays.toString(alive));
			//outfile.flush();
		} catch (Exception e){ }

		if(prevRound == null) // Do not shoot the first round
			return -1;

		updateHistory(prevRound, alive);
		updateRelations(alive);
		double[] expectedShots = getExpectedShots(alive);
		double expectedRetaliation = getExpectedRetaliation(alive) + 0.7;

		if(createLog) try {
			//outfile.println(Arrays.toString(shooter[0]));
			//outfile.println(Arrays.toString(expectedShots));
			outfile.println(Double.toString(expectedRetaliation));
			outfile.flush();
		} catch (Exception e){ }

		// Protect self if possible--only if we are not sure that we are going to die. If we are not sure that we will die, look at other options. .4 added to really make sure that we would die.
		if(shooter[0][id] != -1 && expectedShots[id] < (ELIMINATION_THRESHOLD + 0.4) && allegiance[shooter[0][id]] < 1)
			return shooter[0][id];

		// Protect friends if possible
		int friendId = -1;
		for(int k = 0, i = -1; k < nfriends; k++)
		{
			i = friends[k];
			if(shooter[0][i] != -1 && allegiance[shooter[0][i]] < 1 && ((expectedShots[shooter[0][i]] >= ACTION_THRESHOLD_1 && expectedRetaliation < ELIMINATION_THRESHOLD + 0.3) || (expectedShots[shooter[0][i]] >= ACTION_THRESHOLD_2)) && expectedShots[i] < ELIMINATION_THRESHOLD)
			{
				if(friendId == -1)
					friendId = i;
				//find the best person to target.
				//target an enemy over a neutral.
				//if both are enemies, choose the one that is more likely to die but still less than the ELIMINATION_THRESHOLD
				else if(expectedShots[shooter[0][i]] < ELIMINATION_THRESHOLD)
				{
					if(shooter[0][shooter[0][i]] == id)
						return shooter[0][i];
					else if(allegiance[shooter[0][i]] < allegiance[shooter[0][friendId]])
						friendId = i;
					else if(allegiance[shooter[0][i]] == allegiance[shooter[0][friendId]] && expectedShots[shooter[0][i]] > expectedShots[shooter[0][friendId]])
						friendId = i;
				}
			}
		}
		//TWO action thresholds: action_threshold1 and action_threshold2. use to determine how likely a player is to die. if they are likely to be eliminated, we are not going to shoot them--we have more important things to do.
		//case 1: we are likely to die, shoot someone who will count.
		//case 2: we aren't sure that we are going to die, so we choose a "risky" action that will not cause us to be likely to die in the following round. 

		if(friendId != -1)
			return shooter[0][friendId];

		// Eliminate enemies if possible
		int enemyId = -1;
		for(int k = 0, i = -1; k < nenemies; k++)
		{
			i = enemies[k];
			//if someone is likely to die but isn't super likely to die, we need to cover our ass.
			//might be a good thingto remove and test.
			if(alive[i] && ((expectedShots[i] >= ACTION_THRESHOLD_1 && expectedRetaliation < ELIMINATION_THRESHOLD) || (expectedShots[i] >= ACTION_THRESHOLD_2)))
			{
				if(enemyId == -1)
					enemyId = i;
				else if(expectedShots[i] < ELIMINATION_THRESHOLD)
				{
					//if an enemy is getting shot by a friend (or even a neutral), we should shoot them. otherwise, go for the enemy with the higher expected shots.
					if(shooter[0][enemyId] == -1)
					{
						if(shooter[0][i] != -1 || expectedShots[i] > expectedShots[enemyId])
							enemyId = i;
					}
					else if(shooter[0][i] != -1)
					{
						if(allegiance[shooter[0][i]] > allegiance[shooter[0][enemyId]])
							enemyId = i;
						else if(allegiance[shooter[0][i]] == allegiance[shooter[0][enemyId]] && expectedShots[i] > expectedShots[enemyId])
							enemyId = i;
					}
				}
			}
		}
		if(enemyId != -1)
			return enemyId;
			
		//Endgame strategy--first, calculate numbers below
		double playersAlive = 0, friendsAlive = 0, enemiesAlive = 0;
		int targetId = -1;
		int neutralId = -1;
		for(int i = 0; i < nplayers; i++)
			if(alive[i])
			{
				playersAlive++;
				if(allegiance[i] == 1)
					friendsAlive++;
				else if(allegiance[i] == -1)
				{
					enemiesAlive++;
					targetId = i;
				}
				else if(allegiance[i] == 0)
				{
					neutralId = i;
				}
			}
		//two players that are not our friends (1 of which is ourself, other one is ??)
		if(playersAlive == friendsAlive + 2)
		{
			//even if they aren't doing anything, if they are our enemy, we shoot them
			if(enemiesAlive == 1)
				return targetId;
			else if(nfriends >= nenemies)
			//kill a neutral to reduce everyone's overall score
				return neutralId;
		}
		//we and at least one friend are alive, in addition to two others.
		else if(playersAlive == friendsAlive + 3 && friendsAlive > 0)
		{
			if(enemiesAlive > 0)
				return enemyId;
			//consider testing:
			/* else if (nfriends >= nenemies) return neutralId;*/
		}
		
		//if we got shot while we were doing nothing and are guaranteed to die, shoot one enemy.
		if(shooter[0][id] != -1) // If you are sure to get killed
		{
			if(enemiesAlive > 0)
				return enemyId;
		}

		return -1;
	}


}
