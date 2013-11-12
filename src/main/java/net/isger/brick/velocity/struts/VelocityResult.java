package net.isger.brick.velocity.struts;

import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.isger.brick.velocity.BrickVelocityContext;
import net.isger.brick.velocity.LayoutBean;
import net.isger.brick.velocity.VelocityConstants;

import org.apache.struts2.views.velocity.VelocityManager;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.util.ValueStack;

public class VelocityResult extends
        org.apache.struts2.dispatcher.VelocityResult implements
        VelocityConstants {
    private static final long serialVersionUID = -3630891950185638015L;

    private LayoutBean layout;

    private Template template;

    public VelocityResult() {
        layout = new LayoutBean();
    }

    protected Template getTemplate(ValueStack stack, VelocityEngine velocity,
            ActionInvocation invocation, String location, String encoding)
            throws Exception {
        // 获取主题名称
        String theme = getTheme(stack, velocity);
        // 检查是否支持布局
        String path;
        if (this.layout.isSupport()) {
            // 提取内容页面
            this.template = super.getTemplate(stack, velocity, invocation, "/"
                    + theme + "/" + location, encoding);
            // 获取布局参数
            path = getLayoutPath(stack, velocity);
            location = getLayoutName(stack, velocity);
        } else {
            path = "";
            this.layout.setPath(path);
        }
        // 提取目标模板（布局/内容）
        return super.getTemplate(stack, velocity, invocation, path + "/"
                + theme + "/" + location, encoding);
    }

    protected Context createContext(VelocityManager velocityManager,
            ValueStack stack, HttpServletRequest request,
            HttpServletResponse response, String location) {
        BrickVelocityContext context = new BrickVelocityContext(
                velocityManager.getVelocityEngine(), super.createContext(
                        velocityManager, stack, request, response, location));
        context.getSecretary().setLayout(layout);
        if (this.template != null) {
            // 生成布局模板内容
            StringWriter sw = new StringWriter();
            try {
                this.template.merge(context, sw);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            context.getSecretary().getLayout().setScreen(sw.toString());
        }
        return context;
    }

    public LayoutBean getLayout() {
        return layout;
    }

    private String getTheme(ValueStack stack, VelocityEngine velocity) {
        String theme = this.layout.getTheme();
        if (theme == null) {
            theme = getProperty(stack, velocity, KEY_THEME_DEFAULT,
                    THEME_DEFAULT);
            this.layout.setTheme(theme);
        }
        return theme;
    }

    private String getLayoutPath(ValueStack stack, VelocityEngine velocity) {
        String path = this.layout.getPath();
        if (path == null) {
            path = getProperty(stack, velocity, KEY_LAYOUT_PATH, LAYOUT_PATH);
            this.layout.setPath(path);
        }
        return path;
    }

    private String getLayoutName(ValueStack stack, VelocityEngine velocity) {
        String name = this.layout.getName();
        if (name == null) {
            name = getProperty(stack, velocity, KEY_LAYOUT_DEFAULT,
                    LAYOUT_DEFAULT);
            this.layout.setName(name);
        }
        return name;
    }

    private String getProperty(ValueStack stack, VelocityEngine velocity,
            String key, String def) {
        String val = (String) stack.getContext().get(key);
        if (val == null) {
            val = (String) velocity.getProperty(key);
        }
        if (val == null) {
            val = def;
        }
        return val;
    }

}
