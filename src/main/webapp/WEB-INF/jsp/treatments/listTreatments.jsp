<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="petclinic" tagdir="/WEB-INF/tags" %>

<petclinic:layout pageName="treatments">
    <h2>Treatments</h2>

    <table id="treatmentsTable" class="table table-striped">
        <thead>
        <tr>
            <th>Name</th>
            <th>Description</th>
            <th>Medicines</th>
            <th>Time Limit</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${treatments}" var="treatment">
            <tr>
                <td>
                    <c:out value="${treatment.name}"/>
                </td>
                <td>
                    <c:out value="${treatment.description} "/>
                </td>
                <td>
                	<c:forEach var="medicine" items="${treatment.medicines}">
                        <c:out value="${medicine.name} "/>
                    </c:forEach>
                </td>
               	<td>
               		<c:out value="${treatment.timeLimit}"></c:out>
               	</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</petclinic:layout>