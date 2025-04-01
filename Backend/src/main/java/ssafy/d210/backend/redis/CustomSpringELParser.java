package ssafy.d210.backend.redis;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class CustomSpringELParser {

    /**
     * SpEL을 이용하여 동적으로 키 값을 파싱합니다.
     * @param parameterNames 메서드 파라미터 이름 배열
     * @param args 메서드 파라미터 값 배열
     * @param key SpEL 표현식으로 된 키
     * @return 파싱된 실제 키 값
     */
    public static String getDynamicValue(String[] parameterNames, Object[] args, String key) {
        // 파라미터 이름이나 인자가 null이거나 비어있으면 기본값 반환
        if (parameterNames == null || parameterNames.length == 0 || args == null || args.length == 0) {
            return "defaultKey";  // 또는 적절한 기본값
        }

        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        Expression expression = new SpelExpressionParser().parseExpression(key);
        return expression.getValue(context, String.class);
    }
}
