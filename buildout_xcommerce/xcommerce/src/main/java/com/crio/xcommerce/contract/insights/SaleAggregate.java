
package com.crio.xcommerce.contract.insights;

// import lombok.Getter;
// import lombok.Setter;
import java.util.List;

// @Getter
// @Setter
public class SaleAggregate {

  private Double totalSales;
  private List<SaleAggregateByMonth> aggregateByMonths;

  public Double getTotalSales() {
    return totalSales;
  }

  public void setTotalSales(Double totalSales) {
    this.totalSales = totalSales;
  }

  public List<SaleAggregateByMonth> getAggregateByMonths() {
    return aggregateByMonths;
  }

  public void setAggregateByMonths(List<SaleAggregateByMonth> aggregateByMonths) {
    this.aggregateByMonths = aggregateByMonths;
  }

}
