package org.vaadin.ion.diary.components;

import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;

/**
 * Component for displaying up to three buttons with captions "Save", "Reset" and "Cancel".
 * Listeners must be provided separately.
 *
 * @author Anna Koskinen / Vaadin Ltd.
 */
public class SaveResetCancel extends CssLayout {

    private enum Type {
        SAVE, RESET, CANCEL;
    }

    private Button saveButton;
    private Button resetButton;
    private Button cancelButton;

    public SaveResetCancel() {
        this(true, true, true);
    }

    public SaveResetCancel(boolean displaySave, boolean displayReset, boolean displayCancel) {
        addStyleName("save-reset-cancel");
        if (displaySave) {
            saveButton = createButton(Type.SAVE);
            addComponent(saveButton);
        }
        if (displayReset) {
            resetButton = createButton(Type.RESET);
            addComponent(resetButton);
        }
        if (displayCancel) {
            cancelButton = createButton(Type.CANCEL);
            addComponent(cancelButton);
        }
    }

    private Button createButton(Type type) {
        Button button = new Button();
        String caption;
        switch (type) {
            case SAVE: caption = "Save"; break;
            case RESET: caption = "Reset"; break;
            case CANCEL: caption = "Cancel"; break;
            default: caption = "";
        }
        button.setCaption(caption);
        button.addStyleName(caption.toLowerCase());
        return button;
    }

    public void setSaveClickListener(Button.ClickListener listener) {
        saveButton.addClickListener(listener);
    }

    public void setResetClickListener(Button.ClickListener listener) {
        resetButton.addClickListener(listener);
    }

    public void setCancelClickListener(Button.ClickListener listener) {
        cancelButton.addClickListener(listener);
    }

    public void removeSaveClickListener(Button.ClickListener listener) {
        saveButton.removeClickListener(listener);
    }

    public void removeResetClickListener(Button.ClickListener listener) {
        resetButton.removeClickListener(listener);
    }

    public void removeCancelClickListener(Button.ClickListener listener) {
        cancelButton.removeClickListener(listener);
    }
}
