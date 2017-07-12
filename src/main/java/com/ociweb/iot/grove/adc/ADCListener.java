/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ociweb.iot.grove.adc;

/**
 *
 * @author huydo
 */
public interface ADCListener {
    void conversionResult(int result);
    void alertStatus(int status);
}
