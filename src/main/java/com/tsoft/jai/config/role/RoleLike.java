package com.tsoft.jai.config.role;

import com.tsoft.jai.client.model.Model;
import com.tsoft.jai.config.Role;
import com.tsoft.jai.config.Session;
import com.tsoft.jai.config.agent.Agent;

public class RoleLike {

    private enum RoleLikeTrait {
        Session,
        Agent,
        Role
    }

    private RoleLikeTrait type;

    private Session session;
    private Agent agent;
    private Role role;

    private Model model;
    private Double temperature;
    private Double topP;
    private String useTools;

    public RoleLike(Session session) {
        this.session = session;
        type = RoleLikeTrait.Session;
    }

    public RoleLike(Agent agent) {
        this.agent = agent;
        type = RoleLikeTrait.Agent;
    }

    public RoleLike(Role role) {
        this.role = role;
        type = RoleLikeTrait.Role;
    }

    public Model getModel() {
        return switch (type) {
            case Session -> session.getModel();
            case Agent -> agent.getModel();
            case Role -> role.getModel();
        };
    }

    public void setModel(Model model) {
        switch (type) {
            case Session -> session.setModel(model);
            case Agent -> agent.setModel(model);
            case Role -> role.setModel(model);
        }
    }

    public Double getTemperature() {
        return switch (type) {
            case Session -> session.getTemperature();
            case Agent -> agent.getConfig().getTemperature();
            case Role -> role.getTemperature();
        };
    }

    public void setTemperature(Double temperature) {
        switch (type) {
            case Session -> session.setTemperature(temperature);
            case Agent -> agent.getConfig().setTemperature(temperature);
            case Role -> role.setTemperature(temperature);
        }
    }

    public Double getTopP() {
        return switch (type) {
            case Session -> session.getTopP();
            case Agent -> agent.getConfig().getTopP();
            case Role -> role.getTopP();
        };
    }

    public void setTopP(Double topP) {
        switch (type) {
            case Session -> session.setTopP(topP);
            case Agent -> agent.getConfig().setTopP(topP);
            case Role -> role.setTopP(topP);
        }
    }

    public String getUseTools() {
        return switch (type) {
            case Session -> session.getUseTools();
            case Agent -> agent.getConfig().getUseTools();
            case Role -> role.getUseTools();
        };
    }

    public void setUseTools(String useTools) {
        switch (type) {
            case Session -> session.setUseTools(useTools);
            case Agent -> agent.getConfig().setUseTools(useTools);
            case Role -> role.setUseTools(useTools);
        }
    }
}
