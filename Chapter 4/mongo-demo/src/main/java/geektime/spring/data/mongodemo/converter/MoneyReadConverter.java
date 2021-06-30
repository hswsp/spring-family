package geektime.spring.data.mongodemo.converter;

import org.bson.Document;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.core.convert.converter.Converter;

/**
 * create by: Simon
 * description:
 * Money类型到文档的转换
 * create time: 6/30/21 4:02 PM
 * @return a
 * @Param: null
 */
public class MoneyReadConverter implements Converter<Document, Money> {

    @Override
    public Money convert(Document source) {
        Document money = (Document) source.get("money");
        double amount = Double.parseDouble(money.getString("amount"));
        String currency = ((Document) money.get("currency")).getString("code");
        return Money.of(CurrencyUnit.of(currency), amount);
    }
}
