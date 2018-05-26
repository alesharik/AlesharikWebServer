/*
 *  This file is part of AlesharikWebServer.
 *
 *     AlesharikWebServer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AlesharikWebServer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AlesharikWebServer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
import React from 'react';

export default Checkbox;

export class Checkbox extends React.Component {
    render() {
        return <div className="checkbox-container">
            <p>{this.props.label}</p>
            <div className="checkbox">
                <input id={this.props.id} type="checkbox" onChange={this.props.onChange}
                       defaultChecked={this.props.defaultChecked} data-name={this.props.name}/>
                <label htmlFor={this.props.id}/>
            </div>
        </div>
    }
}

Checkbox.defaultProps = {
    label: "",
    onChange: () => {
    },
    defaultChecked: () => {
    },
    name: ""
};