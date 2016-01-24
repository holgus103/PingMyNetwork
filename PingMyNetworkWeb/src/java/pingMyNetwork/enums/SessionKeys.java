/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pingMyNetwork.enums;

import pingMyNetwork.controller.PingController;

/**
 * @author Jakub Suchan
 * @version %I%, %G%
 * @since 1.0
 */
public enum SessionKeys {

    /**
     * Session key name for waiting
     */
    isWaiting,

    /**
     * Session key name for last recently used IP
     */
    usedIP,

    /**
     * Session key name for the controller
     */
    PingController
}
