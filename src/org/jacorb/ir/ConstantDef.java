package org.jacorb.ir;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-98  Gerald Brose.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import java.lang.reflect.*;
import java.util.*;

import org.jacorb.orb.TypeCode;
import org.jacorb.util.Debug;

import org.omg.CORBA.Any;


public class ConstantDef 
    extends Contained
    implements org.omg.CORBA.ConstantDefOperations
{
    protected static char 	    fileSeparator = 
        System.getProperty("file.separator").charAt(0);
    
    private Field                       field;
    private org.omg.CORBA.TypeCode      typeCode;
    private org.omg.CORBA.IDLType       type_def;
    private org.jacorb.orb.Any		value;
    private org.omg.CORBA.ConstantDescription description;
    private boolean defined = false;
    org.omg.CORBA.Contained             myContainer;

    /**
     * Constructor to create constants defined with an interface
     */

    public ConstantDef( Field field, 
                        org.omg.CORBA.Container _defined_in,
                        org.omg.CORBA.Repository _containing_repository )
    {
        def_kind = org.omg.CORBA.DefinitionKind.dk_Constant;
        containing_repository = _containing_repository;
        defined_in = _defined_in;
        this.field = field;

        name( field.getName());
        version = "1.0";

        myContainer = 
            org.omg.CORBA.ContainedHelper.narrow( defined_in );

        org.jacorb.util.Debug.assert( myContainer != null , 
                                  "Constant should be in an interface!");

        String def_in_id = myContainer.id();
        id = def_in_id.substring(0,def_in_id.lastIndexOf(":")) + 
            "/" + name + ":" + version;

        absolute_name = myContainer.absolute_name() + "::" + name;
        typeCode = TypeCodeUtil.getTypeCode( field.getType(), null );
        org.jacorb.util.Debug.output( 2, "New ConstantDef " + absolute_name());
    }


    /**
     * Constructor to create constants mapped to a separate class
     */

    public ConstantDef( Class c, 
                        org.omg.CORBA.Container _defined_in,
                        org.omg.CORBA.Repository ir )
    {
        def_kind = org.omg.CORBA.DefinitionKind.dk_Constant;
        containing_repository = ir;
        defined_in = _defined_in;
        myContainer = org.omg.CORBA.ContainedHelper.narrow( defined_in );
        try
        { 
            field = c.getDeclaredField("value");
            version = "1.0";
            String classId = c.getName();
            if( classId.indexOf('.') > 0 ) 
            {
                name( classId.substring( classId.lastIndexOf('.')+1));
                String path = classId.substring(0, classId.lastIndexOf('.'));
				id = "IDL:" + path.replace('.','/') + "/" + name + ":" + version;
				absolute_name = myContainer.absolute_name() + "::" + name;
            } 
            else 
            {
                name( classId );
                defined_in = containing_repository;
                id = "IDL:" + name + ":" + version;
                absolute_name = "::" + name;
            }	
            typeCode = TypeCodeUtil.getTypeCode( field.getType(), null );
        } 
        catch ( Exception e )
        {
            e.printStackTrace();
            throw new org.omg.CORBA.INTF_REPOS( ErrorMsg.IR_Not_Implemented,
                                               org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        }
        org.jacorb.util.Debug.output( 2, "New ConstantDef " + absolute_name());
    }

    void define()
    {
        org.jacorb.util.Debug.output( 2, "ConstantDef " + absolute_name() + " defining.");
        value = (org.jacorb.orb.Any) orb.create_any();
        type_def = IDLType.create( typeCode, containing_repository );
        Debug.assert( typeCode != null, "typeCode null!");
        Debug.assert( type_def != null, "type_def null!");

        try 
        { 
            value.insert_object(typeCode, 
                                orb, 
                                field.get(null) );
        } 
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        defined = true;
    }

    public org.omg.CORBA.TypeCode type()
    {
        return typeCode;
    }

    public org.omg.CORBA.Any value()
    {
        return value;
    }

    public void value( org.omg.CORBA.Any _value)
    {
        value = (org.jacorb.orb.Any)_value;
    }

    public org.omg.CORBA.IDLType type_def()
    {
        return type_def;
    }

    public void type_def(org.omg.CORBA.IDLType a)
    {
        type_def = a;
    }

    org.omg.CORBA.ConstantDescription describe_constant()
    {
        Debug.assert( defined , "ConstantDef " + name + " not defined!");
        if( description == null )
        {
            String def_in = null;
            if( myContainer == null )
                def_in = "Global";
            else
                def_in = myContainer.id();
            description = 
                new org.omg.CORBA.ConstantDescription( name, 
                                                       id, 
                                                       def_in, 
                                                       version, 
                                                       typeCode, 
                                                       value);
        }
        return description;
    }

    // from Contained

    public org.omg.CORBA.ContainedPackage.Description describe()
    {
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.CORBA.ConstantDescriptionHelper.insert( a, describe_constant() );
        return new org.omg.CORBA.ContainedPackage.Description( 
                     org.omg.CORBA.DefinitionKind.dk_Constant, a);
    }

    // from IRObject

    public void destroy(){}


}




