package ca.cmput301f13t03.adventure_datetime.view.treeView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.graphics.Path;

public final class ConnectionPlacer 
{
	public int m_horizontalOffset = 0; // TODO::JT set as public for testing purposes
	public int m_verticalOffset = 0; // TODO::JT set as public for testing purposes
	private int m_mapWidth = 0;
	private int m_mapHeight = 0;
	private int m_actualHeight = 0;
	private int m_actualWidth = 0;
	private int m_gridSize = 0;
	public boolean[][] m_map = null; // TODO::JT set as public for testing purposes

	public ConnectionPlacer(ArrayList<GridSegment> gridSegments, int gridSize)
	{
		// expand the gridSegments into a large square region. Fill in
		// any holes as just empty regions
		int leftMost = Integer.MAX_VALUE;
		int rightMost = Integer.MIN_VALUE;
		int topMost = Integer.MAX_VALUE;
		int bottomMost = Integer.MIN_VALUE;
		m_gridSize = gridSize;

		for(GridSegment seg : gridSegments)
		{
			int top = seg.y;
			int bottom = seg.y + seg.height;
			int left = seg.x;
			int right = seg.x + seg.width;

			leftMost = Math.min(left, leftMost);
			rightMost = Math.max(right, rightMost);
			topMost = Math.min(top, topMost);
			bottomMost = Math.max(bottom, bottomMost);
		}

		m_actualWidth = rightMost - leftMost;
		m_actualHeight = bottomMost - topMost;

		m_mapWidth = m_actualWidth / gridSize;
		m_mapHeight = m_actualHeight / gridSize;

		m_horizontalOffset = leftMost;
		m_verticalOffset = topMost;

		m_map = new boolean[m_mapWidth][m_mapHeight];
		FillMap(gridSegments);
	}

	private void FillMap(ArrayList<GridSegment> segments)
	{
		ClearMap();

		for(GridSegment seg : segments)
		{
			ApplySegmentToMap(seg);
		}
	}
	
	private void ApplySegmentToMap(GridSegment seg)
	{
		int xStart = seg.x;
		int yStart = seg.y;
		int increment = m_gridSize;
		
		int xEnd = seg.x + seg.width;
		int yEnd = seg.y + seg.height;
		
		for(int x = xStart ; x < xEnd ; x += increment)
		{
			for(int y = yStart ; y < yEnd ; y += increment)
			{
				if(seg.IsEmpty(x, y))
				{
					int localX = (x - m_horizontalOffset) / m_gridSize;
					int localY = (y - m_verticalOffset) / m_gridSize;
					m_map[localX][localY] = true;
				}
			}
		}
	}
	
	private void ApplyFragmentToMap(FragmentNode node, boolean valueToApply)
	{
		int xStart = node.x - m_horizontalOffset;
		int yStart = node.y - m_verticalOffset;
		int increment = m_gridSize;
		
		int xEnd = xStart + node.width;
		int yEnd = yStart + node.height;
		
		for(int x = xStart ; x < xEnd ; x += increment)
		{
			for(int y = yStart ; y < yEnd ; y += increment)
			{
				int localX = (x) / m_gridSize;
				int localY = (y) / m_gridSize;
				
				m_map[localX][localY] = valueToApply;
			}
		}
	}

	private void ClearMap()
	{
		for(int x = 0 ; x < m_mapWidth ; ++x)
		{
			for(int y = 0 ; y < m_mapHeight ; ++y)
			{
				m_map[x][y] = false;
			}
		}
	}

	public void PlaceConnection(FragmentConnection connection, FragmentNode originFrag, FragmentNode targetFrag)
	{
		// Transform the x and y cords to local map space
		Location start = new Location(	originFrag.x - m_horizontalOffset + originFrag.width / 2, 
										originFrag.y - m_verticalOffset + originFrag.height / 2);
		Location end = new Location(	targetFrag.x - m_horizontalOffset + targetFrag.width / 2, 
										targetFrag.y - m_verticalOffset + targetFrag.height / 2);

		// first remove the origin and target fragments from the collision map
		ApplyFragmentToMap(originFrag, false);
		ApplyFragmentToMap(targetFrag, false);
		
		// Path values
		Path finalPath = GetPath(start, end, new FullPathBuilder(start, end, m_gridSize));
		
		// now restore the collision map
		ApplyFragmentToMap(originFrag, true);
		ApplyFragmentToMap(targetFrag, true);
		
		// transform them to global space and assign it to the connection
		finalPath.offset(m_horizontalOffset, m_verticalOffset);
		connection.SetPath(finalPath);
	}

	// helper function, why does Java not have this???
	private int Clamp(int val, int min, int max)
	{
		return Math.min(Math.max(val, min), max);
	}

	private Path GetPath(Location start, Location target, FullPathBuilder builder)
	{
		SortedLocationList openList = new SortedLocationList(m_map, m_gridSize);
		Set<Location> closedList = new HashSet<Location>();
		int currentDepth = 0;

		LocationNode startPoint = new LocationNode(start, target, currentDepth, null);
		openList.add(startPoint);

		while(openList.size() > 0)
		{
			List<LocationNode> nextBatch = openList.GetAndRemoveNextLowestWeights();

			for(LocationNode currentLocation : nextBatch)
			{
				// only check it if we haven't checked it already
				if(!closedList.contains(currentLocation.location))
				{
					if(builder.TestForDestination(currentLocation.location))
					{
						return builder.BuildPath(currentLocation);
					}
					else
					{
						// no luck, this node is empty.
						// add it to the closedList
						closedList.add(currentLocation.location);

						int baseX = currentLocation.location.x;
						int baseY = currentLocation.location.y;

						// get adjacent locations
						int yUp = baseY - m_gridSize;
						int yDown = baseY + m_gridSize;
						int xRight = baseX + m_gridSize;
						int xLeft = baseX - m_gridSize;

						// clamp all values
						yUp = Clamp(yUp, 0, m_actualHeight - m_gridSize);
						yDown = Clamp(yDown, 0, m_actualHeight - m_gridSize);
						xRight = Clamp(xRight, 0, m_actualWidth - m_gridSize);
						xLeft = Clamp(xLeft, 0, m_actualWidth - m_gridSize);

						// construct adjacent nodes
						LocationNode up = new LocationNode(new Location(baseX, yUp), target, currentDepth, currentLocation);
						LocationNode down = new LocationNode(new Location(baseX, yDown), target, currentDepth, currentLocation);
						LocationNode left = new LocationNode(new Location(xLeft, baseY), target, currentDepth, currentLocation);
						LocationNode right = new LocationNode(new Location(xRight, baseY), target, currentDepth, currentLocation);
						
						// add all adjacent nodes to this list
						AddToClosedList(up, openList, closedList);
						AddToClosedList(down, openList, closedList);
						AddToClosedList(left, openList, closedList);
						AddToClosedList(right, openList, closedList);
						
						// now lets keep searching!
					}
				}
			}

			currentDepth += m_gridSize;
		}

		// if we've made it this far then no path could be found as one does not exist
		// so just return a default path
		return builder.BuildDefaultPath();
	}
	
	private void AddToClosedList(LocationNode node, SortedLocationList openList, Set<Location> closedList)
	{
		if(!closedList.contains(node.location))
		{
			if(!openList.add(node))
			{
				closedList.add(node.location);
			}
		}
	}

	private final class Location implements Comparable<Location>
	{
		public Location(int x, int y)
		{
			this.x = x;
			this.y = y;
		}

		public int x = 0;
		public int y = 0;
		
		public int DistanceSquared(Location other)
		{
			return 	(this.x - other.x)*(this.x - other.x) +
					(this.y - other.y)*(this.y - other.y);
		}

		public int compareTo(Location other) 
		{
			// bleh, I don't like this
			if(this.x > other.x)
			{
				return 1;
			}
			else if(this.x < other.x)
			{
				return -1;
			}
			else
			{
				if(this.y > other.y)
				{
					return 1;
				}
				else if(this.y < other.y)
				{
					return -1;
				}
				else
				{
					// they are equal!
					return 0;
				}
			}
		}
		
		public boolean equals(Object other)
		{
			if(other instanceof Location)
			{
				Location otherLocation = (Location)(other);
				return 	otherLocation.x == this.x &&
						otherLocation.y == this.y;
						
			}
			else
			{
				return false;
			}
		}
		
		public int hashCode()
		{
			return this.x ^ this.y;
		}
	}

	// no I am not using tuple for this.
	// Item1 Item2 is a terrible interface
	private final class LocationNode
	{
		public LocationNode(Location src, Location target, int expansionLevel, LocationNode previousNode)
		{
			location = src;
			weight = (int) Math.round(Math.sqrt(src.DistanceSquared(target)));
			weight += expansionLevel;

			this.prev = previousNode;
		}

		public Location location;
		public int weight;
		public LocationNode prev;

		// used in path creation
		public float dx;
		public float dy;
	}

	private final class SortedLocationList extends LinkedList<LocationNode>
	{
		private static final long serialVersionUID = -1181516011560588762L;
		
		private boolean[][] m_map = null;
		private int m_gridSize = 0;
		
		public SortedLocationList(boolean[][] map, int gridSize)
		{
			this.m_gridSize = gridSize;
			this.m_map = map;
		}

		public boolean add(LocationNode object)
		{
			// only add it if it doesn't collide with anything
			int x = object.location.x / this.m_gridSize;
			int y = object.location.y / this.m_gridSize;
			if(m_map[x][y]) // if there is something at this position 
			{
				return false;
			}
			
			Iterator<LocationNode> itr = this.listIterator();
			int insertLocation = 0;

			while(itr.hasNext())
			{
				if(itr.next().weight > object.weight)
				{
					// found it!
					break;
				}
				++insertLocation;
			}

			super.add(insertLocation, object);

			return true;
		}

		public List<LocationNode> GetAndRemoveNextLowestWeights()
		{
			List<LocationNode> lowest = new ArrayList<LocationNode>();

			LocationNode absoluteLowest = this.getFirst();

			Iterator<LocationNode> itr = this.listIterator();

			while(itr.hasNext())
			{
				LocationNode current = itr.next();
				// sorted list so no need to use less than
				if(current.weight == absoluteLowest.weight)
				{
					lowest.add(current);
				}
				else
				{
					// then we have all we need
					break;
				}
			}

			// now remove those we have collected
			this.removeAll(lowest);

			return lowest;
		}
	}

	private final class FullPathBuilder
	{
		Location m_target = null;
		Location m_start = null;
		int m_targetVariance = 0;

		public FullPathBuilder(Location start, Location target, int targetVariance)
		{
			this.m_start = start;
			this.m_target = target;
			this.m_targetVariance = (int) Math.round(targetVariance * 1.5); // technically the value is only needed to be sqrt(2), but this is close enough
		}

		public boolean TestForDestination(Location loc) 
		{
			return m_target.DistanceSquared(loc) <= m_targetVariance;
		}

		public Path BuildPath(LocationNode endNode) 
		{
			List<LocationNode> pathNodes = new ArrayList<LocationNode>();

			// Backtrack through the nodes until we are back at the beginning (ie prev == null)
			LocationNode current = endNode;

			while(current.prev != null)
			{
				pathNodes.add(current);
				current = current.prev;
			}

			return ConstructFromNodes(pathNodes);
		}

		// TODO::JT this needs to be re evaluated! Right now it just draws straight lines...
		private Path ConstructFromNodes(List<LocationNode> nodes)
		{
			Path result = new Path();
			
			// first simplify the node list
			nodes = SimplifyPath(nodes);
			
			// Code courtesy of stack overflow
			// http://stackoverflow.com/questions/8287949/android-how-to-draw-a-smooth-line-following-your-finger/8289516#8289516
			// --Thanks to : johncarl
			if(nodes.size() > 1)
			{
				for(int i = nodes.size() - 2; i < nodes.size(); i++)
				{
					if(i >= 0)
					{
						LocationNode point = nodes.get(i);

						if(i == 0)
						{
							LocationNode next = nodes.get(i + 1);
							point.dx = ((next.location.x - point.location.x) / 3);
							point.dy = ((next.location.y - point.location.y) / 3);
						}
						else if(i == nodes.size() - 1)
						{
							LocationNode prev = nodes.get(i - 1);
							point.dx = ((point.location.x - prev.location.x) / 3);
							point.dy = ((point.location.y - prev.location.y) / 3);
						}
						else
						{
							LocationNode next = nodes.get(i + 1);
							LocationNode prev = nodes.get(i - 1);
							point.dx = ((next.location.x - prev.location.x) / 3);
							point.dy = ((next.location.y - prev.location.y) / 3);
						}
					}
				}
			}
			
			if(nodes.size() > 0)
			{
				LocationNode first = nodes.get(0);
				result.moveTo(first.location.x, first.location.y);
			}

			for(int i = 1; i < nodes.size(); i++)
			{
				LocationNode point = nodes.get(i);
				LocationNode prev = null;
				prev = nodes.get(i - 1);
				
				result.cubicTo(	prev.location.x + prev.dx,
								prev.location.y + prev.dy,
								point.location.x - point.dx,
								point.location.y - point.dy,
								point.location.x,
								point.location.y);
			}
			
			return result;
		}

		public Path BuildDefaultPath() 
		{
			Path result = new Path();

			result.moveTo(m_start.x, m_start.y);
			result.lineTo(m_target.x, m_target.y);

			return result;
		}
		
		private List<LocationNode> SimplifyPath(List<LocationNode> baseNodes)
		{
			final float VARIANCE = 0.1f;
			
			List<LocationNode> simplifiedList = new ArrayList<LocationNode>();
			
			LocationNode currentNode = null;
			if(baseNodes.size() > 0)
			{
				currentNode = baseNodes.get(0);
				simplifiedList.add(currentNode);
				
				// dir as in direction
				float currentXDir = 0.0f;
				float currentYDir = 0.0f;
				
				for(LocationNode next : baseNodes)
				{
					float nextXdir = next.location.x - currentNode.location.x;
					float nextYdir = next.location.y - currentNode.location.y;
					
					// normalize
					double distance = Math.sqrt(next.location.DistanceSquared(currentNode.location));
					nextXdir /= distance;
					nextYdir /= distance;
					
					// compare
					if(	Math.abs(nextXdir - currentXDir) > VARIANCE ||
						Math.abs(nextYdir - currentYDir) > VARIANCE)
					{
						// then they are different enough
						// for this one to be included
						currentNode = next;
						simplifiedList.add(next);
						
						currentXDir = nextXdir;
						currentYDir = nextYdir;
					}
					
					// else we discard this node and move on
				}
				
				if(baseNodes.size() > 0)
				{
					// ensure the last one is in the list, no matter what
					simplifiedList.add(baseNodes.get(baseNodes.size() - 1));
				}
			}
			
			return simplifiedList;
		}
	}
}
