package org.vaadin.ion.diary.views.addworkoutview;

import javax.inject.Inject;

import org.vaadin.ion.diary.domain.Workout;
import org.vaadin.ion.diary.mvp.AbstractView;
import org.vaadin.ion.diary.presenters.AddWorkoutPresenter;
import org.vaadin.ion.diary.views.AddWorkoutView;

import com.vaadin.cdi.DesktopQualifier;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;

/**
 * Desktop implementation placeholder for {@link AddWorkoutView}.
 *
 * @author Anna Koskinen / Vaadin Ltd.
 */
@DesktopQualifier
public class DesktopAddWorkoutView extends AbstractView<AddWorkoutPresenter, AddWorkoutView, ComponentContainer> implements AddWorkoutView {

    private AddWorkoutPresenter presenter;

    public DesktopAddWorkoutView() {
        super(new CssLayout());
        ((CssLayout)getCompositionRoot()).addComponent(new Label("placeholder view"));
    }

    @Override
    public AddWorkoutPresenter getPresenter() {
        return presenter;
    }

    @Inject
    public void setPresenter(AddWorkoutPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void initialize(Workout workout) {
        // TODO Auto-generated method stub

    }

    @Override
    public Workout commitChanges() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void discardChanges() {
        // TODO Auto-generated method stub

    }

    @Override
    public void displayCommitError() {
        // TODO Auto-generated method stub

    }

}
