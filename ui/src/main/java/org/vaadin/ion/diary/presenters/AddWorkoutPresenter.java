package org.vaadin.ion.diary.presenters;

import org.vaadin.ion.diary.domain.Workout;
import org.vaadin.ion.diary.mvp.AbstractPresenter;
import org.vaadin.ion.diary.views.AddWorkoutView;

/**
 * Presenter for adding a new {@link Workout}.
 *
 * @author Anna Koskinen / Vaadin Ltd.
 */
public class AddWorkoutPresenter extends AbstractPresenter<AddWorkoutPresenter, AddWorkoutView> {

    @Override
    protected void init() {
        getView().initialize(new Workout());
    }

    /**
     * Save workout.
     */
    public void saveRequested() {
        Workout workout = getView().commitChanges();
        if (workout == null) {
            getView().displayCommitError();
        } else {
            // TODO: save
        }
    }

    /**
     * Clear fields.
     */
    public void discardRequested() {
        getView().discardChanges();
    }

    /**
     * Return to previous view
     */
    public void cancelRequested() {
        // TODO
    }

}
