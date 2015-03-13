package org.alb.state.management.writer.mongodb;

import javax.enterprise.context.RequestScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Leonard
 */
@ManagedBean
@RequestScoped
public class PlayersBean {

    private String name;
    private String surname;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void playerUpperCase(ActionEvent event) {
        name = name.toUpperCase();
        surname = surname.toUpperCase();
    }
}
