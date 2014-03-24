package org.vaadin.ion.diary.views.addworkoutview;

import javax.inject.Inject;

import org.vaadin.ion.diary.components.SaveResetCancel;
import org.vaadin.ion.diary.domain.Workout;
import org.vaadin.ion.diary.mvp.AbstractView;
import org.vaadin.ion.diary.presenters.AddWorkoutPresenter;
import org.vaadin.ion.diary.views.AddWorkoutView;

import com.vaadin.cdi.PhoneQualifier;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

/**
 * Mobile implementation for {@link AddWorkoutView}.
 *
 * @author Anna Koskinen / Vaadin Ltd.
 */
@PhoneQualifier
public class MobileAddWorkoutView extends AbstractView<AddWorkoutPresenter, AddWorkoutView, ComponentContainer> implements AddWorkoutView {

    private AddWorkoutPresenter presenter;
    private BeanItem<Workout> workoutItem;
    private BeanFieldGroup<Workout> workoutFieldGroup;
    private Label errorLabel;
    private SaveResetCancel buttonLayout;

    public MobileAddWorkoutView() {
        super(new CssLayout());
    }

    public MobileAddWorkoutView(ComponentContainer layout) {
        super(layout);
        addStyleName("add-workout-view");
        addStyleName("mobile-add-workout-view");
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
    protected CssLayout getCompositionRoot() {
        return (CssLayout) super.getCompositionRoot();
    }

    @Override
    public void initialize(Workout workout) {
        workoutItem = new BeanItem<Workout>(workout);
        workoutFieldGroup = new BeanFieldGroup<Workout>(Workout.class);
        workoutFieldGroup.setItemDataSource(workoutItem);
        errorLabel = new Label();
        buttonLayout = new SaveResetCancel();
        buttonLayout.setSaveClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                presenter.saveRequested();
            }
        });
        buttonLayout.setResetClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                presenter.discardRequested();
            }
        });
        buttonLayout.setCancelClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                presenter.cancelRequested();
            }
        });
        displayView();
    }

    /**
     * Prepare the view for displaying.
     */
    private void displayView() {
        getCompositionRoot().removeAllComponents();
        errorLabel.setValue(null);
        for (Object propertyId : workoutFieldGroup.getUnboundPropertyIds()) {
            Field<?> field = workoutFieldGroup.buildAndBind(propertyId);
            if (field instanceof TextField) {
                ((TextField) field).setNullRepresentation("");
            }
            field.setRequired(true);
            addComponent(field);
        }
        addComponent(errorLabel);
        addComponent(buttonLayout);
    }

    @Override
    public Workout commitChanges() {
        try {
            workoutFieldGroup.commit();
            return workoutItem.getBean();
        } catch (CommitException e) {
            // TODO: logger.error("Workout commit failed.", e);
            System.out.println("Workout commit failed.");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void discardChanges() {
        workoutFieldGroup.discard();
    }

    @Override
    public void displayCommitError() {
        errorLabel.setValue("Commit failed, check values.");
    }

}
