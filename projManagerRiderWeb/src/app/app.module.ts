import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { RouterModule } from '@angular/router';

import { AppComponent } from './app.component';
import { AdminLayoutComponent } from './layouts/admin-layout/admin-layout.component';
import { AuthLayoutComponent } from './layouts/auth-layout/auth-layout.component';

import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { AppRoutingModule } from './app.routing';
import { ComponentsModule } from './components/components.module';
import { StoresComponent } from './pages/stores/stores.component';
import { RidersComponent } from './pages/riders/riders.component';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';


@NgModule({
    imports: [
        BrowserAnimationsModule,
        FormsModule,
        HttpClientModule,
        ComponentsModule,
        NgbModule,
        RouterModule,
        AppRoutingModule,
        FontAwesomeModule
    ],
  declarations: [
    AppComponent,
    AdminLayoutComponent,
    AuthLayoutComponent,
    StoresComponent,
    RidersComponent
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
