package tddc17;

import aima.core.environment.liuvacuum.*;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.*;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.Random;

// Class for keeping x, y coordinates
class Coordinate {
	public int x;
	public int y;

	public Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}

	// Returns true if two coordinates are the same, otherwise false
	public boolean equals(Coordinate c) {
		return (c.x == this.x && c.y == this.y);
	}
}

class MyAgentState {
	public int[][] world = new int[30][30];
	public int initialized = 0;
	final int UNKNOWN = 0;
	final int WALL = 1;
	final int CLEAR = 2;
	final int DIRT = 3;
	final int HOME = 4;
	final int ACTION_NONE = 0;
	final int ACTION_MOVE_FORWARD = 1;
	final int ACTION_TURN_RIGHT = 2;
	final int ACTION_TURN_LEFT = 3;
	final int ACTION_SUCK = 4;

	public int agent_x_position = 1;
	public int agent_y_position = 1;
	public int agent_last_action = ACTION_NONE;

	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	public int agent_direction = EAST;

	// Setup states in a 2D array. Home is set to [0, 0] and every other cell is set
	// to unknown
	MyAgentState() {
		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world[i].length; j++) {
				world[i][j] = UNKNOWN;
			}

			world[1][1] = HOME;
			agent_last_action = ACTION_NONE;
		}
	}

	// Based on the last action and the received percept updates the x & y agent
	public void updatePosition(DynamicPercept p) {
		Boolean bump = (Boolean) p.getAttribute("bump");

		if ((agent_last_action == ACTION_MOVE_FORWARD) && !bump) {

			switch (agent_direction) {
			case MyAgentState.NORTH:
				agent_y_position--;
				break;
			case MyAgentState.EAST:
				agent_x_position++;
				break;
			case MyAgentState.SOUTH:
				agent_y_position++;
				break;
			case MyAgentState.WEST:
				agent_x_position--;
				break;
			}
		}
	}

	public void updateWorld(int x_position, int y_position, int info) {
		world[x_position][y_position] = info;
	}

	public void printWorldDebug() {
		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world[i].length; j++) {

				if (world[j][i] == UNKNOWN)
					System.out.print(" ? ");

				if (world[j][i] == WALL)
					System.out.print(" # ");

				if (world[j][i] == CLEAR)
					System.out.print(" . ");

				if (world[j][i] == DIRT)
					System.out.print(" D ");

				if (world[j][i] == HOME)
					System.out.print(" H ");
			}

			System.out.println("");
		}
	}
}

class MyAgentProgram implements AgentProgram {

	private int initnialRandomActions = 10;
	private Random random_generator = new Random();

	// Here you can define your variables!
	private Coordinate homePosition = new Coordinate(-1, -1);

	// initialize a stack of Coordinatess
	private Stack<Coordinate> route = new Stack<Coordinate>();

	public int iterationCounter = 20 * 20 * 5;
	public MyAgentState state = new MyAgentState();

	// Computes a breadth-first search algorithm which
	// searches for the coordinates to the closest unknown coordinate or home.
	private void BFS(boolean returnHome) {
		Queue<Coordinate> queue = new LinkedList<Coordinate>();
		Coordinate startCoordinate = new Coordinate(state.agent_x_position, state.agent_y_position);
		boolean[][] visited = new boolean[30][30];
		Coordinate[][] previousStep = new Coordinate[30][30];
		boolean foundPlan = false;
		Coordinate targetCoordinate = new Coordinate(-1, -1);

		queue.add(startCoordinate);
		previousStep[startCoordinate.x][startCoordinate.y] = new Coordinate(-1, -1);

		while (!queue.isEmpty()) {
			Coordinate position = queue.remove();

			visited[position.x][position.y] = true;

			boolean goalCheck = (returnHome ? position.equals(homePosition) : state.world[position.x][position.y] == state.UNKNOWN);

			if (goalCheck && !position.equals(startCoordinate)) {
				foundPlan = true;
				targetCoordinate = new Coordinate(position.x, position.y);
				break;
			}
			
			int[] dx = { 0, 1, 0, -1 };
			int[] dy = { -1, 0, 1, 0 };

			for (int i = 0; i < 4; i++) {
			    int neighborX = position.x + dx[i];
			    int neighborY = position.y + dy[i];

			    if (state.world[neighborX][neighborY] != state.WALL && !visited[neighborX][neighborY]) {
					queue.add(new Coordinate(neighborX, neighborY));
					previousStep[neighborX][neighborY] = new Coordinate(position.x, position.y);
				}
			}			
		}

		if (foundPlan) {
			Coordinate templateCoordinate = targetCoordinate;

			while (true) {
				// startCoordinate is always current coordinate?
				if (templateCoordinate.equals(startCoordinate)) {
					break;

				} else {
					route.push(templateCoordinate);
					templateCoordinate = previousStep[templateCoordinate.x][templateCoordinate.y];
				}
			}
		}
	}

	private Action decideAndRunAction(Coordinate currentPosition, Coordinate goalPosition) {
		// Calculate the target direction
		int targetDirection = getTargetDirection(currentPosition, goalPosition);

		// Calculate the turn direction needed to align with the target direction
		int turnDirection = (targetDirection - state.agent_direction + 4) % 4;

		// Take action based on the calculated turn direction
		if (turnDirection == 1 || turnDirection == 2) { // Turn right
			state.agent_direction = (state.agent_direction + 1) % 4;
			state.agent_last_action = state.ACTION_TURN_RIGHT;
			return LIUVacuumEnvironment.ACTION_TURN_RIGHT;

		} else if (turnDirection == 3) { // Turn left
			state.agent_direction = (state.agent_direction + 3) % 4;
			state.agent_last_action = state.ACTION_TURN_LEFT;
			return LIUVacuumEnvironment.ACTION_TURN_LEFT;

		} else { // Move forward
			route.pop();
			state.agent_last_action = state.ACTION_MOVE_FORWARD;
			return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
		}
	}

	// Helper function to calculate the target direction
	private int getTargetDirection(Coordinate currentPosition, Coordinate goalPosition) {
		int dx = goalPosition.x - currentPosition.x;
		int dy = goalPosition.y - currentPosition.y;

		if (Math.abs(dx) >= Math.abs(dy)) {
			// Horizontal movement
			if (dx > 0) {
				return MyAgentState.EAST;

			} else {
				return MyAgentState.WEST;
			}

		} else {
			// Vertical movement
			if (dy > 0) {
				return MyAgentState.SOUTH;

			} else {
				return MyAgentState.NORTH;
			}
		}
	}

	// moves the Agent to a random start position
	// uses percepts to update the Agent position - only the position, other
	// percepts are ignored
	// returns a random action
	private Action moveToRandomStartPosition(DynamicPercept percept) {

		int action = random_generator.nextInt(6); // 0-5
		initnialRandomActions--;
		state.updatePosition(percept);

		if (action == 0) { // rotate counter clockwise (left)
			state.agent_direction = ((state.agent_direction - 1) % 4);

			if (state.agent_direction < 0)
				state.agent_direction += 4;

			state.agent_last_action = state.ACTION_TURN_LEFT;
			return LIUVacuumEnvironment.ACTION_TURN_LEFT;

		} else if (action == 1) {
			state.agent_direction = ((state.agent_direction + 1) % 4);

			state.agent_last_action = state.ACTION_TURN_RIGHT;
			return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
		}

		state.agent_last_action = state.ACTION_MOVE_FORWARD;
		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
	}

	@Override
	public Action execute(Percept percept) {

		// DO NOT REMOVE this if condition!!!
		if (initnialRandomActions > 0) {
			return moveToRandomStartPosition((DynamicPercept) percept);

		} else if (initnialRandomActions == 0) {
			// process percept for the last step of the initial random actions
			initnialRandomActions--;
			state.updatePosition((DynamicPercept) percept);
			System.out.println("Processing percepts after the last execution of moveToRandomStartPosition()");
			state.agent_last_action = state.ACTION_SUCK;
			return LIUVacuumEnvironment.ACTION_SUCK;
		}

		// This example agent program will update the internal agent state while only
		// moving forward.
		// START HERE - code below should be modified!

		// State update based on the percept value and the last action
		state.updatePosition((DynamicPercept) percept);

		iterationCounter--;

		if (iterationCounter == 0) {
			state.printWorldDebug();
			System.out.print("Iterations ran out");
			return NoOpAction.NO_OP;
		}

		// Updates current percept
		DynamicPercept p = (DynamicPercept) percept;
		Boolean bump = (Boolean) p.getAttribute("bump");
		Boolean dirt = (Boolean) p.getAttribute("dirt");
		Boolean home = (Boolean) p.getAttribute("home");

		// Update world if the agent bumps
		if (bump) {
			switch (state.agent_direction) {
			case MyAgentState.NORTH:
				state.updateWorld(state.agent_x_position, state.agent_y_position - 1, state.WALL);
				break;
			case MyAgentState.EAST:
				state.updateWorld(state.agent_x_position + 1, state.agent_y_position, state.WALL);
				break;
			case MyAgentState.SOUTH:
				state.updateWorld(state.agent_x_position, state.agent_y_position + 1, state.WALL);
				break;
			case MyAgentState.WEST:
				state.updateWorld(state.agent_x_position - 1, state.agent_y_position, state.WALL);
				break;
			}
		}

		// Update world if dirt
		if (dirt) {
			state.updateWorld(state.agent_x_position, state.agent_y_position, state.DIRT);

		} else {
			state.updateWorld(state.agent_x_position, state.agent_y_position, state.CLEAR);
		}

		if (home) {
			homePosition = new Coordinate(state.agent_x_position, state.agent_y_position);
		}

		// store the current position of the agent
		Coordinate currentPosition = new Coordinate(state.agent_x_position, state.agent_y_position);

		// Next action selection based on the percept value
		if (dirt) {
			System.out.println("DIRT -> choosing SUCK action!");
			state.agent_last_action = state.ACTION_SUCK;
			return LIUVacuumEnvironment.ACTION_SUCK;

		} else {

			// Search for unknown positions
			if (route.isEmpty()) {
				BFS(false);

				// Check again if route is empty
				if (route.isEmpty()) {

					// If agent is home, end program
					if (currentPosition.equals(homePosition)) {
						state.printWorldDebug();
						System.out.println("Finsihed!");
						return NoOpAction.NO_OP;

						// Whole map is explores, return home
					} else {
						BFS(true);
					}
				}
			}
			
			return decideAndRunAction(currentPosition, route.peek());
		}
	}
}

public class MyVacuumAgent extends AbstractAgent {
	public MyVacuumAgent() {
		super(new MyAgentProgram());
	}
}