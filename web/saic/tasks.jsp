<%@page contentType="application/xhtml+xml" pageEncoding="UTF-8"%>
<%@taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="sarariman" uri="/WEB-INF/tlds/sarariman" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<jsp:useBean beanName="sarariman" id="sarariman" scope="application" type="com.stackframe.sarariman.Sarariman" />
<html xmlns="http://www.w3.org/1999/xhtml">
    <sql:setDataSource var="db" dataSource="jdbc/sarariman"/>
    <head>
        <link href="../style.css" rel="stylesheet" type="text/css"/>
        <title>SAIC Tasks</title>
    </head>
    <body>
        <p><a href="../">Home</a></p>

        <!-- FIXME: Get customer id out of config or look it up or something. -->
        <sql:query dataSource="jdbc/sarariman" var="tasks">
            SELECT t.id, t.name, t.project, t.billable, s.task, s.charge_number
            FROM tasks AS t
            LEFT OUTER JOIN projects AS p ON p.id = t.project
            LEFT OUTER JOIN customers AS c ON c.id = p.customer
            LEFT OUTER JOIN saic_tasks AS s ON s.task = t.id
            WHERE c.id = 1
            ORDER BY t.id ASC
        </sql:query>

        <h1>SAIC Tasks</h1>

        <h2>Map a new task</h2>
        <form action="task.jsp" method="POST">
            <label for="task_id">Task: </label>
            <select id="task_id" name="task_id">
                <c:forEach var="task" items="${tasks.rows}">
                    <c:if test="${task.task == null}">
                        <option value="${task.id}">${fn:escapeXml(task.name)} (${task.id})</option>
                    </c:if>
                </c:forEach>
            </select>
            <input type="submit" value="Create" name="create"/>
        </form>

        <h2>Tasks</h2>
        <table>
            <tr><th>ID</th><th>Name</th><th>Project</th><th>Charge Number</th></tr>
            <c:forEach var="task" items="${tasks.rows}">
                <c:if test="${task.task != null}">
                    <tr>
                        <td><a href="task.jsp?task_id=${task.id}">${task.id}</a></td>
                        <td><a href="task.jsp?task_id=${task.id}">${fn:escapeXml(task.name)}</a></td>
                        <td><a href="task.jsp?task_id=${task.id}">${fn:escapeXml(sarariman:project(sarariman, task.project).name)}</a></td>
                        <td><a href="task.jsp?task_id=${task.id}">${task.charge_number}</a></td>
                    </tr>
                </c:if>
            </c:forEach>
        </table>
        <%@include file="../footer.jsp" %>
    </body>
</html>
