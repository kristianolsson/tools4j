/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deephacks.tools4j.config.internal.admin.jsf;

import java.io.IOException;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.deephacks.tools4j.config.admin.AdminContext;
import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.model.Schema;
import org.deephacks.tools4j.support.event.AbortRuntimeException;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

@Named(value = "adminTree")
@SessionScoped
public class JsfAdminTree extends DefaultTreeNode {
    private static final long serialVersionUID = 6266276692145772015L;
    private TreeNode selectedNode;
    private DefaultTreeNode root;

    public JsfAdminTree() {
        super();
    }

    public void onRightClickEdit() {
        if (selectedNode == null) {
            return;
        }
        Node selectedBean = (Node) selectedNode.getData();
        if (!selectedBean.hasAdminBean()) {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Illegal action",
                    "Choose a bean and then 'edit' to modify beans."));
            return;
        }
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("edit.xhtml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onRightClickCreate() {
        if (selectedNode == null) {
            return;
        }
        Node selectedBean = (Node) selectedNode.getData();
        if (selectedBean.hasAdminBean()) {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Illegal action",
                    "Choose a schema and then 'create' to add beans."));
            return;
        }
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("create.xhtml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onRightClickDelete() {
        if (selectedNode == null) {
            return;
        }
        Node selectedBean = (Node) selectedNode.getData();
        if (!selectedBean.hasAdminBean()) {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Illegal action",
                    "Choose a bean and then 'delete' to remove beans."));
            return;
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        try {
            AdminContext.get().delete(selectedBean.bean.getBean().getId());
            fc.addMessage(null, new FacesMessage("Operation successful", "Bean was deleted."));
            clearCache();
        } catch (AbortRuntimeException e) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error occured", e
                    .getEvent().getMessage()));
        }
    }

    /**
     * Called when a node in the tree is selected.
     * 
     * <p:tree> selectedNode 
     */
    public void setSelectedNode(TreeNode selectedNode) {
        if (selectedNode == null) {
            return;
        }

        this.selectedNode = selectedNode;
    }

    public TreeNode getSelectedNode() {
        return this.selectedNode;
    }

    /**
     * Called when the page is rendered to initialize the tree.
     * 
     * <p:tree> value
     */
    public DefaultTreeNode getRoot() {
        if (root != null) {
            return root;
        }
        root = new DefaultTreeNode("Root", null);
        AdminContext ctx = AdminContext.get();
        for (Schema s : ctx.getSchemas().values()) {
            DefaultTreeNode schema = new DefaultTreeNode(new Node(new JsfAdminBean(s, this)), root);
            List<Bean> beans = ctx.list(s.getName());
            for (Bean bean : beans) {
                new DefaultTreeNode(new Node(new JsfAdminBean(bean, this)), schema);
            }
        }
        return root;
    }

    public JsfAdminBean getSelectedAdminBean() {
        if (selectedNode == null) {
            return null;
        }
        return ((Node) selectedNode.getData()).bean;
    }

    /**
     * Is need only for rendering the name of an admin bean correctly in the tree.
     */
    private static class Node {
        public JsfAdminBean bean;
        private String name;

        public Node(JsfAdminBean bean) {
            this.bean = bean;
            if (bean.getBean() == null) {
                // this is an schema that does not yet have inititalized a bean  
                this.name = bean.getSchema().getName();
            } else {
                this.name = bean.getBean().getId().getInstanceId();
            }

        }

        public boolean hasAdminBean() {
            return bean.getBean() != null;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public void clearCache() {
        selectedNode = null;
        root = null;
    }

}
