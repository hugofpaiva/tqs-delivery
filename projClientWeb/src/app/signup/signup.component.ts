import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {AccountService} from '../services/account/account.service';
import {AlertService} from '../services/alert/alert.service';
import {first} from 'rxjs/operators';

@Component({
    selector: 'app-signup',
    templateUrl: './signup.component.html',
    styleUrls: ['./signup.component.scss']
})
export class SignupComponent implements OnInit {
    form: FormGroup;
    loading = false;
    submitted = false;

    constructor(private formBuilder: FormBuilder,
                private route: ActivatedRoute,
                private router: Router,
                private accountService: AccountService,
                private alertService: AlertService) {
    }

    ngOnInit() {
        this.form = this.formBuilder.group({
            email: ['', [Validators.required, Validators.email]],
            name: ['', [Validators.required]],
            password: ['', [Validators.required, Validators.minLength(8)]]
        });
    }


    // convenience getter for easy access to form fields
    get f() {
        return this.form.controls;
    }

    onSubmit() {
        this.submitted = true;

        // reset alerts on submit
        this.alertService.clear();

        // stop here if form is invalid
        if (this.form.invalid) {
            return;
        }

        this.loading = true;
        this.accountService.register(this.f.email.value, this.f.password.value, this.f.name.value)
            .pipe(first())
            .subscribe({
                next: () => {
                    this.loading = false;
                    this.router.navigateByUrl('/login');
                    this.alertService.success('Client was created!');

                },
                error: error => {
                    this.alertService.error('Client could not be created!');
                    this.loading = false;
                }
            });
    }
}
