Feature: Choose product in the Ozon.ru online store,
  and compares its name and price with the one in the cart.

  Scenario: The user searches for a product and compares its name and price with the one in the cart
    Given User is logged into Ozon.ru site
    And User searches a product on homepage
    And User can see item name and price in search result
    And User add product to cart
    When User go to cart
    Then User sees the same item name and price
