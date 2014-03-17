/*
 * Copyright 2014 Fabien Michel
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.message;

import madkit.kernel.Message;


/**
 * Instances of classes that implement this interface are used to
 * filter messages when consulting the mailbox.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.4
 * @version 1
 */
public interface MessageFilter {

   /**
    * Tests if a specified message matches the requirement.
    *
    * @param   m   the message to test.
    * @return  <code>true</code> if and only if the message matches the requirement.
    */
public boolean accept(Message m);

}
