<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="petclinic" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>

<petclinic:layout pageName="vets">

    <h2>Vet Information</h2>


    <table class="table table-striped">
        <tr>
            <th>Name</th>
            <td><b><c:out value="${vet.firstName} ${vet.lastName}"/></b></td>
        </tr>
        <tr>
            <th>Address</th>
            <td><c:out value="${vet.address}"/></td>
        </tr>
        <tr>
            <th>City</th>
            <td><c:out value="${vet.city}"/></td>
        </tr>
        <tr>
            <th>Telephone</th>
            <td><c:out value="${vet.telephone}"/></td>
        </tr>
        <tr>
            <th>Username</th>
            <td><c:out value="${vet.user.username}"/></td>
        </tr>
        <tr>
        	<th>Specialties</th>
        	<td>
        	<c:forEach var="specialty" items="${vet.specialties}">
            	<c:out value="- ${specialty.name} "/>
            </c:forEach>
            <c:if test="${vet.nrOfSpecialties == 0}">none</c:if>
            </td>
        </tr>
    </table>
    
    <spring:url value="/vets/{vetId}/edit" var="editUrl">
        <spring:param name="vetId" value="${vet.id}"/>
    </spring:url>
    <a href="${fn:escapeXml(editUrl)}" class="btn btn-default">Edit Vet</a>
    <br>
    <sec:authorize access="hasAuthority('admin')">
    	<a href="/appointments/${vet.id}">Show vet appointments</a>
    </sec:authorize>
    
</petclinic:layout>