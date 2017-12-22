/**
 * 
 */
package com.ach.achViewer.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ilyakharlamov
 *
 */
public class ACHEditorModel {
    private boolean achFileDirty;
    
    private String title;

    private List<ModelSubscriber> subscribers;

    public ACHEditorModel() {
        super();
        this.subscribers = new ArrayList<>();
    }

    public boolean isAchFileDirty() {
        return achFileDirty;
    }

    public void setAchFileDirty(boolean achFileDirty) {
        this.achFileDirty = achFileDirty;
        for (ModelSubscriber subscriber: subscribers) {
            subscriber.onFileDirty();
        }
    }

    /**
     * @param fileName
     */
    public void setTitle(String title) {
       this.title = title;
    }
    
    public String getTitle() {
        return title;
    }

    /**
     * 
     */
    public void addSubscriber(ModelSubscriber subscriber) {
        this.subscribers.add(subscriber);
    }
}
