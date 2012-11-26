<%--
  Copyright (C) 2012 StackFrame, LLC
  This code is licensed under GPLv2.
--%>

<%@taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!-- FIXME: Validate begin and end. -->

<sql:update dataSource="jdbc/sarariman">
    UPDATE company_events
    SET begin = ?, end = ?, name = ?, location = ?, description = ?
    WHERE id = ?
    <sql:param value="${param.begin}"/>
    <sql:param value="${param.end}"/>
    <sql:param value="${param.name}"/>
    <sql:param value="${param.location}"/>
    <sql:param value="${param.description}"/>
    <sql:param value="${param.id}"/>
</sql:update>
<c:redirect url="../#events"/>