package org.vaadin.ion.diary.views;

import org.vaadin.ion.diary.domain.Workout;
import org.vaadin.ion.diary.mvp.View;
import org.vaadin.ion.diary.presenters.AddWorkoutPresenter;

/**
 * View interface for adding a {@link Workout}.
 *
 * @author Anna Koskinen / Vaadin Ltd.
 */
public interface AddWorkoutView extends View<AddWorkoutPresenter, AddWorkoutView> {

    /**
     * Initialize view.
     *
     * @param workout the workout that is to be modified
     */
    void initialize(Workout workout);

    /**
     * Commit changes to the modified workout.
     *
     * @return updated workout
     */
    Workout commitChanges();

    /**
     * Discard changes to the modified workout.
     */
    void discardChanges();

    /**
     * Display error message for a failed commit.
     */
    void displayCommitError();
}
