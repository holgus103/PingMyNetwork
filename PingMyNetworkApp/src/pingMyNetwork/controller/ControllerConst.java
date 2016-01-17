/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pingMyNetwork.controller;

/**
 * @version     %I%, %G%
 * @since       1.0
 * @author Jakub Suchan
 */
public interface ControllerConst {

    /**
     * Value used for handshakes
     */
    static final int handShakeVal = 13378888;

    /**
     * Value indicating an success header
     */
    static final int successVal = 200;

    /**
     * Value indicating a failure header
     */
    static final int failureVal = 400;

    /**
     *  Port used for connections
     */
    static final int PORT = 9989;
}