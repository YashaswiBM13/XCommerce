
package com.crio.xcommerce.contract.insights;

// import lombok.AllArgsConstructor;
// import lombok.Getter;
// import lombok.Setter;

// @AllArgsConstructor
// @Getter
// @Setter
public class SaleAggregateByMonth {

  private Integer month;
  private Double sales;

  public SaleAggregateByMonth(Integer month, Double sales) {
    this.month = month;
    this.sales = sales;
  }

  public Integer getMonth() {
    return month;
  }

  public Double getSales() {
    return sales;
  }

  public void setMonth(Integer month) {
    this.month = month;
  }

  public void setSales(Double sales) {
    this.sales = sales;
  }
}
