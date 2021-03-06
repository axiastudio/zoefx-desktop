/*
 * Copyright (c) 2014, AXIA Studio (Tiziano Lattisi) - http://www.axiastudio.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the AXIA Studio nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY AXIA STUDIO ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL AXIA STUDIO BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.axiastudio.zoefx.desktop.db;

import javafx.beans.property.Property;

import java.util.*;

/**
 * User: tiziano
 * Date: 26/05/14
 * Time: 20:52
 */
public class TimeMachine {

    private List<Map<Property, Object>> snapshots = new ArrayList<>();

    public void resetAndCreateSnapshot(Collection<Property> properties){
        reset();
        createSnapshot(properties);
    }

    public void createSnapshot(Collection<Property> properties){
        Map<Property, Object> snapshot = new HashMap<>();
        for( Property property: properties ){
            snapshot.put(property, property.getValue());
        }
        snapshots.add(snapshot);
    }

    public void undo(){
        int last = snapshots.size() - 1;
        Map<Property, Object> snapshot = snapshots.get(last);
        snapshots.remove(last);
        for(Property property: snapshot.keySet() ){
            property.setValue(snapshot.get(property));
        }
    }

    public void rollback(){
        Map<Property, Object> snapshot = snapshots.get(0);
        for(Property property: snapshot.keySet() ){
            property.setValue(snapshot.get(property));
        }
        snapshots.clear();
    }

    public void reset(){
        snapshots.clear();
    }

}
