/**
 *
 */
package org.tn5250j;

/**
 * @author Vyacheslav Soldatov &lt;vyacheslav.soldatov@inbox.ru&gt;
 *
 */
public class ConnectUser {

    private String user;
    private String password;
    private String library;
    private String initialMenu;
    private String program;

    /**
     * @param userName user name.
     */
    public void setUser(final String userName) {
        user = userName;
    }

    /**
     * @return user name.
     */
    public String getUser() {
        return user;
    }

    /**
     * @param pwd password.
     */
    public void setPassword(final String pwd) {
        this.password = pwd;
    }

    /**
     * @return password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param lib library.
     */
    public void setLibrary(final String lib) {
        this.library = lib;
    }

    /**
     * @return library
     */
    public String getLibrary() {
        return library;
    }

    /**
     * @param menu initial menu.
     */
    public void setInitialMenu(final String menu) {
        this.initialMenu = menu;
    }

    /**
     * @return initial menu.
     */
    public String getInitialMenu() {
        return initialMenu;
    }

    /**
     * @param prg program.
     */
    public void setProgram(final String prg) {
        this.program = prg;
    }

    /**
     * @return program.
     */
    public String getProgram() {
        return program;
    }
}
