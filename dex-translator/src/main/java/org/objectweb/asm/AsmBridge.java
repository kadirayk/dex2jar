/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2014 Panxiaobo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.objectweb.asm;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.objectweb.asm.tree.MethodNode;

public class AsmBridge {
    public static MethodVisitor searchMethodWriter(MethodVisitor mv) {
        while (mv != null && !(mv instanceof MethodWriter)) {
            mv = mv.mv;
        }
        return mv;
    }

    public static int sizeOfMethodWriter(MethodVisitor mv) {
        MethodWriter mw = (MethodWriter) mv;
        return mw.computeMethodInfoSize();
    }

    private static void removeMethodWriter(MethodWriter mw, ClassWriter cw) {
        // mv must be the last element
        MethodWriter p = getFirstMethod(cw);
        if (p == mw) {
            setFirstMethod(cw, null);
            if (getLastMethod(cw) == mw) {
                setLastMethod(cw, null);
            }
        } else {
            while (p != null) {
                if (p.mv == mw) {
                    p.mv = mw.mv;
                    if (getLastMethod(cw) == mw) {
                        setLastMethod(cw, p);
                    }
                    break;
                } else {
                    p = (MethodWriter) p.mv;
                }
            }
        }
    }

    private static MethodWriter getFirstMethod(ClassWriter cw){
        MethodWriter mw = null;
        try {
            mw = (MethodWriter) FieldUtils.readField(cw, "firstMethod", true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return mw;
    }

    private static void setFirstMethod(ClassWriter cw, Object value){
        try {
            FieldUtils.writeField(cw, "firstMethod", value, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static MethodWriter getLastMethod(ClassWriter cw){
        MethodWriter mw = null;
        try {
            mw = (MethodWriter) FieldUtils.readField(cw, "lastMethod", true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return mw;
    }

    private static void setLastMethod(ClassWriter cw, Object value){
        try {
            FieldUtils.writeField(cw, "lastMethod", value, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void replaceMethodWriter(MethodVisitor mv, MethodNode mn) {
        MethodWriter mw = (MethodWriter) mv;
        ClassWriter cw = getClassWriter(mw);
        mn.accept(cw);
        removeMethodWriter(mw, cw);
    }

    private static ClassWriter getClassWriter(MethodWriter mw) {
        ClassWriter cw = null;
        SymbolTable symbolTable = null;
        try {
            symbolTable = (SymbolTable) FieldUtils.readField(mw, "symbolTable", true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        cw = symbolTable.classWriter; // mw.cw;
        return cw;
    }
}

