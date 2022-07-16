import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

// Using The Zalik and Clapworthy algorithm 
// http://citeseerx.ist.psu.edu/viewdoc/download;jsessionid=DE73270AD29AC5CC08802C9341E34A23?doi=10.1.1.11.4259&rep=rep1&type=pdf

final class V {
	public Vector3f p;
	public enum Type {
		MAX, MIN, HMIN, HMAX
	};
	public Type type;
	
	public V(Vector3f p) {
		this.p = p;
	}
}

public class Trapezoidation {
	public static void convert(List<Vector3f> contour) {
		// Sort contour-points based on y position
		LinkedList<Vector3f> contourSorted = new LinkedList<>(contour);
		Collections.sort(contourSorted, new Comparator<Vector3f>() {
			@Override
			public int compare(Vector3f v1, Vector3f v2) {
				if (v1.y == v2.y) return 0;
				return v1.y - v2.y < 0.0f ? 1 : -1;
			}
		});

		// Create horizontal sublists
		LinkedList<LinkedList<V>> horizontalSubLists = new LinkedList<>();
		{
			LinkedList<V> sublist = null;
			for (int i=0; i<contourSorted.size(); i++) {
				Vector3f p = contourSorted.get(i);
				// Classify
				V v = new V(p);
				Vector3f pMinus = contourSorted.get((i-1 + contourSorted.size()) % contourSorted.size());
				Vector3f pPlus = contourSorted.get((i+1) % contourSorted.size());

				if (pMinus.y <= p.y && pPlus.y <= p.y) {
					v.type = V.Type.MAX;
				}
				else if (pMinus.y > p.y && pPlus.y > p.y) {
					v.type = V.Type.MIN;
				}
				else if (pMinus.y >= p.y && pPlus.y <= p.y) {
					v.type = V.Type.HMIN;
				}
				else if (pMinus.y <= p.y && pPlus.y >= p.y) {
					v.type = V.Type.HMAX;
				}
				else {
					throw new RuntimeException("unhandled type: " + pMinus.y + " | " + p.y + " | " + pPlus.y);
				}
				
				if (sublist == null) {
					sublist = new LinkedList<>();
					sublist.add(v);
				}
				else {
					float yOfCurrentList = sublist.get(0).p.y;
					if (p.y != yOfCurrentList) {
						horizontalSubLists.add(sublist);
						sublist = new LinkedList<>();
					}
					sublist.add(v);
				}
			}
			if (sublist.size() != 0)  horizontalSubLists.add(sublist);
		}
		// Sort sublist based on x-coordinate
		/*
		for (LinkedList<Vector3f> sublist : horizontalSubLists) {
			Collections.sort(sublist, new Comparator<Vector3f>() {
				@Override
				public int compare(Vector3f v1, Vector3f v2) {
					if (v1.y == v2.y) return 0;
					return v1.x - v2.x < 0.0f ? 1 : -1;
				}
			});
		}
		*/
		
		// Sort first line
		Collections.sort(horizontalSubLists.get(0), new Comparator<V>() {
			@Override
			public int compare(V v1, V v2) {
				if (v1.p.y == v2.p.y) return 0;
				return v1.p.x - v2.p.x < 0.0f ? 1 : -1;
			}
		});
		/*
		System.out.println("====");
		for (LinkedList<Vector3f> sublist : horizontalSubLists) {
			
			for (Vector3f p : sublist) {
				System.out.print(p);
			}
			System.out.println("");
		}*/

	}
}

/*
 * More to read:
 * http://www.coordinators.cis.usouthal.edu/~hain/general/Publications/CISST%2705_Trapezoidation.pdf
 * http://sigbjorn.vik.name/projects/Triangulation.pdf
 * https://graphics.stanford.edu/courses/cs268-09-winter/notes/handout6.pdf
 */
