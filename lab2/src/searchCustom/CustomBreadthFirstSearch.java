package searchCustom;

public class CustomBreadthFirstSearch  extends CustomGraphSearch {

	public CustomBreadthFirstSearch(int maxDepth){
		
// 		calls the constructor of the extended class
// 		* no problem of trapping into infinite loops
//		* optimal for finding the shortest path
		
//		insertFront = false
		super(false); // Temporary random choice, you need to pick true or false!
		System.out.println("Change line above in \"CustomBreadthFirstSearch.java\"!");
	}
};
