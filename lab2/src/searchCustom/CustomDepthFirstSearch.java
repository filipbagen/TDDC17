package searchCustom;

public class CustomDepthFirstSearch extends CustomGraphSearch {

	public CustomDepthFirstSearch(int maxDepth) {

// 		calls the constructor of the extended class.
// 		* can be stuck in infinite loop
//		* not optimal for finding the shortest path

//		insertFront = true
		super(true); // Temporary random choice, you need to true or false!
		System.out.println("Change line above in \"CustomDepthFirstSearch.java\"!");
	}
};
