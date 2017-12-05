/**************************************************************************************
 https://camel-extra.github.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation; either version 3
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.


 You should have received a copy of the GNU Lesser General Public
 License along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 02110-1301, USA.

 http://www.gnu.org/licenses/lgpl-3.0-standalone.html
 ***************************************************************************************/
package org.apache.camel.component.firebase.exception;

import com.google.firebase.database.DatabaseError;

/**
 * In case Firebase throws a database error, this object wraps a Throwable around the original object.
 */
public class DatabaseErrorException extends RuntimeException {

    private final DatabaseError databaseError;

    public DatabaseErrorException(DatabaseError databaseError) {
        super();
        this.databaseError = databaseError;
    }
}
