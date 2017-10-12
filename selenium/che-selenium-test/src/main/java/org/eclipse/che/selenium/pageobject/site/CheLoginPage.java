/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject.site;

import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.constant.TestTimeoutsConstants;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Dmytro Nochevnov */
@Singleton
public class CheLoginPage implements LoginPage {

  @FindBy(name = "username")
  private WebElement usernameInput;

  @FindBy(name = "password")
  private WebElement passwordInput;

  @FindBy(name = "login")
  private WebElement loginButton;

  public void login(String username, String password, SeleniumWebDriver seleniumWebDriver) {
    PageFactory.initElements(seleniumWebDriver, this);
    waitOnOpen(seleniumWebDriver);
    usernameInput.clear();
    usernameInput.sendKeys(username);
    passwordInput.clear();
    passwordInput.sendKeys(password);
    loginButton.click();
    waitOnClose(seleniumWebDriver);
  }

  public void waitOnOpen(SeleniumWebDriver seleniumWebDriver) {
    PageFactory.initElements(seleniumWebDriver, this);
    new WebDriverWait(seleniumWebDriver, TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(loginButton));
  }

  public void waitOnClose(SeleniumWebDriver seleniumWebDriver) {
    PageFactory.initElements(seleniumWebDriver, this);
    new WebDriverWait(seleniumWebDriver, TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfAllElements(ImmutableList.of(loginButton)));
  }

  public boolean isOpened(SeleniumWebDriver seleniumWebDriver) {
    PageFactory.initElements(seleniumWebDriver, this);
    try {
      waitOnOpen(seleniumWebDriver);
    } catch (TimeoutException e) {
      return false;
    }

    return true;
  }
}
