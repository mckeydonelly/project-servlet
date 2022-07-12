package com.tictactoe.servlet;

import com.tictactoe.model.Field;
import com.tictactoe.model.Sign;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;


@WebServlet(name = "LogicServlet", value = "/logic")
public class LogicServlet extends HttpServlet {

    public static final String INDEX_PAGE = "/index.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession currentSession = req.getSession();
        Field field = extractField(currentSession);
        int index = getSelectedIndex(req);

        Sign currentSign = field.getField().get(index);
        if (Sign.EMPTY != currentSign) {
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(INDEX_PAGE);
            dispatcher.forward(req, resp);
            return;
        }

        field.getField().put(index, Sign.CROSS);
        if (checkWin(resp, currentSession, field)) {
            return;
        }

        int emptyFieldIndex = field.getEmptyFieldIndex();

        if (emptyFieldIndex >= 0) {
            field.getField().put(emptyFieldIndex, Sign.NOUGHT);
            if (checkWin(resp, currentSession, field)) {
                return;
            }
        } else {
            currentSession.setAttribute("draw", true);
            List<Sign> data = field.getFieldData();
            currentSession.setAttribute("data", data);
            resp.sendRedirect(INDEX_PAGE);
            return;
        }

        List<Sign> data = field.getFieldData();

        currentSession.setAttribute("data", data);
        currentSession.setAttribute("field", field);

        resp.sendRedirect(INDEX_PAGE);
    }

    private Field extractField(HttpSession currentSession) {
        Object fieldAttribute = currentSession.getAttribute("field");
        if (Field.class != fieldAttribute.getClass()) {
            currentSession.invalidate();
            throw new RuntimeException("Session is broken, try one more time");
        }
        return (Field) fieldAttribute;
    }

    private int getSelectedIndex(HttpServletRequest request) {
        String click = request.getParameter("click");
        boolean isNumeric = click.chars().allMatch(Character::isDigit);
        return isNumeric ? Integer.parseInt(click) : 0;
    }

    private boolean checkWin(HttpServletResponse response, HttpSession currentSession, Field field) throws IOException {
        Sign winner = field.checkWin();
        if (Sign.CROSS == winner || Sign.NOUGHT == winner) {
            currentSession.setAttribute("winner", winner);

            List<Sign> data = field.getFieldData();

            currentSession.setAttribute("data", data);

            response.sendRedirect(INDEX_PAGE);
            return true;
        }
        return false;
    }
}