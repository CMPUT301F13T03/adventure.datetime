/*
 * Copyright (c) 2013 Andrew Fontaine, James Finlay, Jesse Tucker, Jacob Viau, and
 * Evan DeGraff
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


package ca.cmput301f13t03.adventure_datetime.controller;

import java.util.HashSet;
import java.util.UUID;
import ca.cmput301f13t03.adventure_datetime.model.Interfaces.IStoryModelDirector;
import ca.cmput301f13t03.adventure_datetime.model.Story;
import ca.cmput301f13t03.adventure_datetime.model.StoryFragment;

/**
 * Controller for aspects of authoring a story
 *
 * @author Evan DeGraff
 * @author Jesse Tucker
 */
public class AuthorController {
	
	private IStoryModelDirector m_storyDirector = null;

    /**
     * Constructor for the AuthorController
     *
     * @param director The director managing transactions with local and online storage
     */
	public AuthorController(IStoryModelDirector director) {
		m_storyDirector = director;
	}

    /**
     * Passes a new story created by the director to the view
     *
     * @return A new Story
     */
	public Story CreateStory()
	{
		return m_storyDirector.CreateNewStory();
	}

    /**
     * Saves a story to the local storage
     * @param story The Story to save
     * @return Whether or not the save was successful
     */
	public boolean saveStory(Story story) {
		return m_storyDirector.putStory(story);
	}

    /**
     * Gets a story from the director
     *
     * @param storyId UUID of the Story to get
     *
     * @return The Story, if found. Null, otherwise.
     */
	public Story getStory(String storyId) {
		return m_storyDirector.getStory(storyId);
	}

    /**
     * Deletes a Story and all StoryFragments from local and online storage.
     *
     * @param storyId UUID of the story to delete
     */
	public void deleteStory(String storyId) {
		Story story = m_storyDirector.getStory(storyId);
		if (story == null) return;
		
		HashSet<UUID> fragments = story.getFragmentIds();

		for(UUID fragment : fragments) {
			deleteFragment(fragment);
		}

		m_storyDirector.deleteStory(storyId);
	}
	
	/**
	 * Deletes a bookmark by StoryID
	 * 
	 * @param storyId UUID of the story to delete
	 */
	public void deleteBookmark(String storyId) {
		m_storyDirector.deleteBookmark(storyId);
	}

    /**
     * Saves a fragment to local storage
     *
     * @param fragment The fragment to save
     *
     * @return Whether or not the fragment was saved successfully
     */
	public boolean saveFragment(StoryFragment fragment) {
		return m_storyDirector.putFragment(fragment);
	}

    /**
     * Deletes a fragment from local or online storage
     *
     * @param fragmentId UUID of the fragment to delete
     */
	public void deleteFragment(UUID fragmentId) {
		m_storyDirector.deleteFragment(fragmentId);
	}

    /**
     * Selects a Story from the local or online storage.
     *
     * @param storyId UUID of the story
     */
	public void selectStory(String storyId)
	{
		m_storyDirector.selectStory(storyId);
	}

    /**
     * Selects a StoryFragment from the local or online storage
     *
     * @param fragmentId UUID of the fragment
     */
	public void selectFragment(String fragmentId)
	{
		m_storyDirector.selectFragment(fragmentId);
	}
	
	/*public void publish(long storyID){
		m_storyDirector.publish(storyID);
	}*/
}
