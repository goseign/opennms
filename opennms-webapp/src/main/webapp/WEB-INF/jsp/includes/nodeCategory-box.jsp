<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page language="java"
        contentType="text/html"
        session="true"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div id="category-box" class="card">
    <div class="card-header">
        <h3 class="card-title">
            Surveillance Category Memberships
            <c:if test="${isAdmin == 'true'}">
                (<a href="<c:url value="admin/categories.htm?edit&amp;node=${param.node}"/>">Edit</a>)
            </c:if>
        </h3>
    </div>
    <div style="max-height: 15em; overflow-x: auto">
        <table class="table table-condensed" style="margin-bottom: 0px">
            <c:if test="${empty categories}">
                <tr>
                    <td>This node is not a member of any categories.</td>
                </tr>
            </c:if>


            <c:forEach items="${categories}" var="category">
                <tr>
                    <td>${category.name}</td>
                </tr>
            </c:forEach>
        </table>
    </div>
</div>
